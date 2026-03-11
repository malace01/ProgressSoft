package com.progresssoft.fxdeals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application bootstrap class.
 *
 * <p>We keep startup wiring intentionally minimal and rely on Spring Boot auto-configuration
 * so runtime behavior remains convention-driven and easy to maintain.</p>
 */
@SpringBootApplication
public class FxDealsWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FxDealsWarehouseApplication.class, args);
    }
}
