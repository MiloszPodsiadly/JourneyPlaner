package com.milosz.podsiadly.uiservice.security;

import com.milosz.podsiadly.uiservice.config.JwtTokenUtil;
import com.milosz.podsiadly.uiservice.service.UserClient;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final SpotifyTokenCache spotifyTokenCache;
    private final UserClient userClient;

    public OAuth2LoginSuccessHandler(JwtTokenUtil jwtTokenUtil,
                                     OAuth2AuthorizedClientService authorizedClientService,
                                     SpotifyTokenCache spotifyTokenCache,
                                     UserClient userClient) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authorizedClientService = authorizedClientService;
        this.spotifyTokenCache = spotifyTokenCache;
        this.userClient = userClient;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String jwt = jwtTokenUtil.generateToken(principal);

        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setSecure(false);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        String spotifyId = principal.getAttribute("id");
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("display_name");

        if (spotifyId != null) {
            Cookie spotifyIdCookie = new Cookie("spotify_id", spotifyId);
            spotifyIdCookie.setPath("/");
            spotifyIdCookie.setMaxAge(86400);
            spotifyIdCookie.setSecure(false);
            spotifyIdCookie.setHttpOnly(false);
            response.addCookie(spotifyIdCookie);

            userClient.createUserIfNotExists(spotifyId, name, email);
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                "spotify", authentication.getName());

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            Instant expiry = authorizedClient.getAccessToken().getExpiresAt();
            long expiresIn = expiry != null ? Duration.between(Instant.now(), expiry).getSeconds() : 3600;

            String refreshToken = authorizedClient.getRefreshToken() != null
                    ? authorizedClient.getRefreshToken().getTokenValue()
                    : null;

            spotifyTokenCache.update(accessToken, refreshToken, (int) expiresIn);
            Cookie spotifyAccessTokenCookie = new Cookie("spotify_access_token", accessToken);
            spotifyAccessTokenCookie.setPath("/");
            spotifyAccessTokenCookie.setSecure(false);
            spotifyAccessTokenCookie.setHttpOnly(false);
            spotifyAccessTokenCookie.setMaxAge((int) expiresIn);
            response.addCookie(spotifyAccessTokenCookie);
        }
        response.sendRedirect("/main-menu");
    }
}
