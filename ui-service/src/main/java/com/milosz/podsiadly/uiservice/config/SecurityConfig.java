package com.milosz.podsiadly.uiservice.config;

import com.milosz.podsiadly.uiservice.security.OAuth2LoginSuccessHandler;
import com.milosz.podsiadly.uiservice.security.OidcLogoutHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           OAuth2LoginSuccessHandler successHandler,
                                           OidcLogoutHandler oidcLogoutHandler) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login/**", "/oauth2/**", "/logged-out").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .cacheControl(Customizer.withDefaults())
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(successHandler)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("jwt", "spotify_access_token", "spotify_id", "JSESSIONID")
                );
        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher sessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
