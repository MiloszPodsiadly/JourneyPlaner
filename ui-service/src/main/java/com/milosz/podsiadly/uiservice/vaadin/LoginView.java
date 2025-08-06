package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Route("login")
@PermitAll
public class LoginView extends VerticalLayout {

    public LoginView(ClientRegistrationRepository clientRegistrationRepository) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(to right, #1DB954, #191414)");
        getStyle().set("color", "white");

        Image logo = new Image("https://upload.wikimedia.org/wikipedia/commons/1/19/Spotify_logo_without_text.svg", "Spotify Logo");
        logo.setHeight("100px");

        H1 heading = new H1("Journey Planner");
        heading.getStyle().set("color", "white");

        Button spotifyLoginButton = new Button("🎧 Login with Spotify", event -> {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("spotify");

            if (registration != null) {
                String loginUrl = "/oauth2/authorization/spotify"; // Spring handles this route
                UI.getCurrent().getPage().setLocation(loginUrl);
            } else {
                Notification.show("Spotify client not configured.", 3000, Notification.Position.MIDDLE);
            }
        });

        spotifyLoginButton.getStyle()
                .set("background-color", "#1DB954")
                .set("color", "white")
                .set("font-size", "18px")
                .set("padding", "12px 24px")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.3)");

        add(logo, heading, spotifyLoginButton);
    }
}
