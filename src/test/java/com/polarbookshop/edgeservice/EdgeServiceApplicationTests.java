package com.polarbookshop.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EdgeServiceApplicationTests {
	/**
	Should you want to disable the session management through Redis in some of your tests,
	you can do so by setting the spring.session.store-type property to none in a specific test
	 class using the @TestPropertySource annotation, or in a property file if you want to make
	it apply to all test classes.

	 **/
	private static final int REDIS_PORT = 6379;
	@Container
	static GenericContainer<?> redis =
			new GenericContainer<>(DockerImageName.parse("redis:7.0"))
					.withExposedPorts(REDIS_PORT);

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host",
				() -> redis.getHost());
		registry.add("spring.redis.port",
				() -> redis.getMappedPort(REDIS_PORT));
	}

	@Test
	void verifyThatSpringContextLoads() {
	}

}
