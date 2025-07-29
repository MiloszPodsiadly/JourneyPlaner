package com.milosz.podsiadly.uiservice.vaadin;


import com.milosz.podsiadly.uiservice.security.SpotifyTokenCache;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Route("logout")
@PermitAll
public class LogoutView {

    @Autowired
    public LogoutView(HttpServletResponse response, SpotifyTokenCache spotifyTokenCache) throws IOException {
        // 1. Clear Vaadin session
        VaadinSession.getCurrent().close();
        UI.getCurrent().getSession().close();

        // 2. Clear Spotify token cache (if singleton)
        spotifyTokenCache.clear(); // You need to create this method

        // 3. Delete JWT cookie
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete immediately
        response.addCookie(cookie);

        // 4. Redirect to home or login
        response.sendRedirect("/oauth2/authorization/spotify");
    }
}

