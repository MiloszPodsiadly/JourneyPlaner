package com.milosz.podsiadly.gatewayservice.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = JwtDecoderConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=super-secret-test-key-for-jwt-that-is-long-enough-32b-min"
})
@DisplayName("🔐 JwtDecoderConfig Integration Tests")
class JwtDecoderConfigTest {

    @Autowired
    private JwtDecoder jwtDecoder;

    private static final String SECRET = "super-secret-test-key-for-jwt-that-is-long-enough-32b-min";

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting JwtDecoderConfig tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] JwtDecoderConfig tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Preparing case...");
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleanup done.");
    }

    @Test
    @DisplayName("🟢 Bean is created")
    void beanCreated() {
        assertThat(jwtDecoder).isNotNull();
    }

    @Test
    @DisplayName("🟢 Decodes a valid HS256 token and exposes claims")
    void decodesValidToken() throws Exception {
        String token = signHs256Token(
                SECRET,
                "user-123",
                "user@example.com",
                Instant.now().plusSeconds(3600)
        );

        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("user-123");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("user@example.com");
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getHeaders().get("alg")).isEqualTo("HS256");
    }

    @Test
    @DisplayName("🔴 Rejects token with invalid signature")
    void rejectsInvalidSignature() throws Exception {
        String token = signHs256Token(
                "another-secret-value-that-doesnt-match",
                "user-123",
                "user@example.com",
                Instant.now().plusSeconds(3600)
        );

        assertThatThrownBy(() -> jwtDecoder.decode(token))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid signature");
    }

    @Test
    @DisplayName("⏳ Rejects expired token (or exp < iat)")
    void rejectsExpiredToken() throws Exception {
        String token = signHs256Token(
                SECRET,
                "user-123",
                "user@example.com",
                Instant.now().minusSeconds(60)
        );

        assertThatThrownBy(() -> jwtDecoder.decode(token))
                .isInstanceOf(JwtException.class)
                .satisfies(ex -> {
                    String msg = ex.getMessage();
                    assertThat(msg).matches("(?s).*?(Jwt expired|expiresAt must be after issuedAt).*");
                });
    }

    private static String signHs256Token(String secret, String sub, String email, Instant exp) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .claim("email", email)
                .issueTime(new Date())
                .expirationTime(Date.from(exp))
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build();
        SignedJWT signedJWT = new SignedJWT(header, claims);

        JWSSigner signer = new MACSigner(secret.getBytes(StandardCharsets.UTF_8));
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}