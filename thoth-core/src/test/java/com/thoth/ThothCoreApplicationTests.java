package com.thoth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ThothCoreApplication.class)
@ActiveProfiles("test")
class ThothCoreApplicationTests {

    @Test
    void contextLoads() {
    }
}
