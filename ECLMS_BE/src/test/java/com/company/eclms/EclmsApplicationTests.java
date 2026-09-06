package com.company.eclms;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class EclmsApplicationTests {

	@TestConfiguration
	static class TestConfig {
		@Bean
		public StringRedisTemplate stringRedisTemplate() {
			return Mockito.mock(StringRedisTemplate.class);
		}
	}

	@Test
	void contextLoads() {
	}

}
