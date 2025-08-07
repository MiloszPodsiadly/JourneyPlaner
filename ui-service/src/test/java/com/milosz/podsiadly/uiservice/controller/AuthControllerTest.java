package com.milosz.podsiadly.uiservice.controller;

import com.milosz.podsiadly.uiservice.config.JwtTokenUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("🔐 AuthController WebMvc Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @TestConfiguration
    static class MockConfig {
        @Bean
        JwtTokenUtil jwtTokenUtil() {
            return mock(JwtTokenUtil.class);
        }
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting AuthController tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] AuthController tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Preparing test case...");
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up...");
    }

    @Test
    @DisplayName("🟢 /auth/success returns JWT when authenticated via oauth2Login()")
    void successReturnsToken() throws Exception {
        when(jwtTokenUtil.generateToken(any())).thenReturn("mock-jwt-token");

        mockMvc.perform(get("/auth/success")
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("id", "spotify-123");
                                    attrs.put("email", "user@example.com");
                                })
                        )
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));

        verify(jwtTokenUtil).generateToken(any());
    }

    @Test
    @DisplayName("🔒 /auth/success without authentication → 3xx redirect (to /login or /oauth2/authorization/spotify)")
    void successRequiresAuthentication() throws Exception {
        var res = mockMvc.perform(get("/auth/success").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse();

        var location = res.getHeader("Location");
        Assertions.assertNotNull(location);
        org.assertj.core.api.Assertions.assertThat(location)
                .matches(".*/(login|oauth2/authorization/[^/]+)$");
    }
}
