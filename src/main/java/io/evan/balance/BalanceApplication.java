package io.evan.balance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BalanceApplication {
    public static void main(String... args) {
        SpringApplication.run(BalanceApplication.class, args);
    }
}
