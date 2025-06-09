package com.cart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.cart.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
class ShoppingCartApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the application context loads successfully
    }
}
