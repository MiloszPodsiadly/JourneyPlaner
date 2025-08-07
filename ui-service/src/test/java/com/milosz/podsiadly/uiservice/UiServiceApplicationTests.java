package com.milosz.podsiadly.uiservice;

import com.milosz.podsiadly.uiservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"jwt.secret=your-256-bit-secret-your-256-bit-secret"
})
@Import(TestSecurityConfig.class)
@SpringBootTest
class UiServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
