package com.milosz.podsiadly.gatewayservice.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        SecurityConfig.class,
        JwtDecoderConfig.class,
        SecurityConfigTest.DummyController.class
})
@Import(SecurityConfigTest.TestBeans.class)
@TestPropertySource(properties = {
        "jwt.secret=super-secret-test-key-for-gateway-that-is-long-enough-32-bytes"
})
@AutoConfigureMockMvc
@DisplayName("🔐 Gateway SecurityConfig – integration")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String SECRET = "super-secret-test-key-for-gateway-that-is-long-enough-32-bytes";

    @TestConfiguration
    static class TestBeans {
        @Bean
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }
    }

    @BeforeEach
    void setUp() { System.out.println("➡️ [BeforeEach] Preparing case..."); }

    @AfterEach
    void tearDown() { System.out.println("⬅️ [AfterEach] Cleanup done."); }

    @Test
    @DisplayName("🟢 /auth/** is public")
    void authEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    @DisplayName("🟢 /login/** is public")
    void loginEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/login/page"))
                .andExpect(status().isOk())
                .andExpect(content().string("login-page"));
    }

    @Test
    @DisplayName("🟢 POST /auth/** works without CSRF")
    void postOnPublicWithoutCsrf() throws Exception {
        mockMvc.perform(post("/auth/echo")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello"));
    }

    @Test
    @DisplayName("🔒 Protected endpoint → 401 without Authorization")
    void protectedWithoutAuthorization() throws Exception {
        var res = mockMvc.perform(get("/api/secure"))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();

        String www = res.getHeader("WWW-Authenticate");
        if (www != null) assertThat(www).containsIgnoringCase("Bearer");
    }

    @Test
    @DisplayName("🔒 Protected endpoint → 401 with malformed Authorization header")
    void protectedWithMalformedAuthHeader() throws Exception {
        mockMvc.perform(get("/api/secure")
                        .header("Authorization", "Token nope"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("🟢 Protected endpoint → 200 with valid HS256 JWT")
    void protectedWithValidJwt() throws Exception {
        String jwt = signHs256("user-123", SECRET, Instant.now(), Instant.now().plusSeconds(900));

        mockMvc.perform(get("/api/secure")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("secured-ok"));
    }

    @Test
    @DisplayName("⏳ Protected endpoint → 401 when token expired")
    void protectedWithExpiredToken() throws Exception {
        String expired = signHs256("user-123", SECRET,
                Instant.now().minusSeconds(3600), Instant.now().minusSeconds(1800));

        mockMvc.perform(get("/api/secure")
                        .header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    private static String signHs256(String sub, String secret, Instant iat, Instant exp) throws Exception {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        JWSSigner signer = new MACSigner(key);

        var claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .issueTime(Date.from(iat))
                .expirationTime(Date.from(exp))
                .claim("scope", "read")
                .build();

        var jwt = new SignedJWT(
                new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256),
                claims
        );
        jwt.sign(signer);
        return jwt.serialize();
    }

    @RestController
    static class DummyController {
        @GetMapping("/auth/ping") public String ping() { return "pong"; }
        @PostMapping("/auth/echo") public String echo(@RequestBody String b){ return b; }
        @GetMapping("/login/page") public String login(){ return "login-page"; }
        @GetMapping("/api/secure") public String secure(){ return "secured-ok"; }
    }
}