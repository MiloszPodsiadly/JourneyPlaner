package com.milosz.podsiadly.gatewayservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"jwt.secret=your-256-bit-secret-your-256-bit-secret"
})
@SpringBootTest
class GatewayServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}