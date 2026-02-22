package com.project.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"api-events"})
@ActiveProfiles("test")
class ApiServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
