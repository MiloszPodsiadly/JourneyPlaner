package com.milosz.podsiadly.uiservice.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OidcLogoutHandler implements LogoutSuccessHandler {

    private final SpotifyTokenCache spotifyTokenCache;

    public OidcLogoutHandler(SpotifyTokenCache spotifyTokenCache) {
        this.spotifyTokenCache = spotifyTokenCache;
    }

    @Override
    public void onLogoutSuccess(jakarta.servlet.http.HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication)
            throws IOException {
        spotifyTokenCache.clear();
        deleteCookie(response, "jwt");
        deleteCookie(response, "spotify_access_token");
        deleteCookie(response, "spotify_id");
        deleteCookie(response, "JSESSIONID");

        response.sendRedirect("/logged-out");
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }
}
