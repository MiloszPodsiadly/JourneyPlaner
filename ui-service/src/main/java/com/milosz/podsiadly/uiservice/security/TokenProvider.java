package com.milosz.podsiadly.uiservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

    private final OAuth2AuthorizedClientService clientService;

    public TokenProvider(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    public String getAccessToken(Authentication authentication) {
        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient("spotify", authentication.getName());
        return client != null ? client.getAccessToken().getTokenValue() : null;
    }
}

