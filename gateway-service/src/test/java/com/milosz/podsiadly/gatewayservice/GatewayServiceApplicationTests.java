package com.milosz.podsiadly.gatewayservice;

import com.milosz.podsiadly.gatewayservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"jwt.secret=your-256-bit-secret-your-256-bit-secret"
})
@Import(TestSecurityConfig.class)
@SpringBootTest
class GatewayServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}