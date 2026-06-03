package com.example.Matchingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.kafka.admin.auto-create=false",
    "spring.kafka.listener.auto-startup=false"
})
class MatchingserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
