package com.milosz.podsiadly.uiservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"jwt.secret=your-256-bit-secret-your-256-bit-secret"
})
@SpringBootTest
class UiServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
