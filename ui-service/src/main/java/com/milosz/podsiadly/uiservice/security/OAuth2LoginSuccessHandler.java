package com.milosz.podsiadly.uiservice.security;

import com.milosz.podsiadly.uiservice.config.JwtTokenUtil;
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

    public OAuth2LoginSuccessHandler(JwtTokenUtil jwtTokenUtil,
                                     OAuth2AuthorizedClientService authorizedClientService,
                                     SpotifyTokenCache spotifyTokenCache) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authorizedClientService = authorizedClientService;
        this.spotifyTokenCache = spotifyTokenCache;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String jwt = jwtTokenUtil.generateToken(principal);

        // ✅ Save JWT in cookie
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        // ✅ Retrieve Spotify tokens and cache them
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                "spotify", authentication.getName());

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            Instant expiry = authorizedClient.getAccessToken().getExpiresAt();
            long expiresIn = expiry != null ? Duration.between(Instant.now(), expiry).getSeconds() : 3600;

            String refreshToken = authorizedClient.getRefreshToken() != null
                    ? authorizedClient.getRefreshToken().getTokenValue()
                    : null;

            // ✅ This is the key line
            spotifyTokenCache.update(accessToken, refreshToken, (int) expiresIn);
        }

        response.sendRedirect("/main-menu");
    }
}
