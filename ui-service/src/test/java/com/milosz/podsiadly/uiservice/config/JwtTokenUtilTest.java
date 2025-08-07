package com.milosz.podsiadly.uiservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("🔐 JwtTokenUtil Unit Tests")
class JwtTokenUtilTest {

    private static final String SECRET =
            "super-secret-test-key-for-jwt-that-is-long-enough-32b-min";

    private JwtTokenUtil jwtTokenUtil;
    private Key signingKey;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting JwtTokenUtil tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] JwtTokenUtil tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up JwtTokenUtil...");
        jwtTokenUtil = new JwtTokenUtil(SECRET);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up...");
        jwtTokenUtil = null;
        signingKey = null;
    }

    @Test
    @DisplayName("🟢 generateToken: contains subject(id), email and valid signature")
    void generateToken_containsExpectedClaimsAndSignature() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("id")).thenReturn("spotify-123");
        when(principal.getAttribute("email")).thenReturn("user@example.com");

        String token = jwtTokenUtil.generateToken(principal);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("spotify-123");
        assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("⏳ generateToken: expiration is ~24h from now (±10s)")
    void generateToken_expirationIs24h() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("id")).thenReturn("id-1");
        when(principal.getAttribute("email")).thenReturn("e@x.com");

        long now = System.currentTimeMillis();
        String token = jwtTokenUtil.generateToken(principal);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long expMillis = claims.getExpiration().getTime();
        long delta = expMillis - now;

        assertThat(delta).isBetween(86_400_000L - 10_000L, 86_400_000L + 10_000L);
    }

    @Test
    @DisplayName("🟠 generateToken: when subject(id) is null, token is created without 'sub' but with email")
    void generateToken_allowsNullIdAndOmitsSubject() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("id")).thenReturn(null);
        when(principal.getAttribute("email")).thenReturn("user@example.com");

        String token = jwtTokenUtil.generateToken(principal);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isNull();
        assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(new Date());
    }
}