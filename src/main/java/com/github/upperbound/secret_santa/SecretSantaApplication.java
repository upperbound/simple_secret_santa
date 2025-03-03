package com.github.upperbound.secret_santa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing
@EnableScheduling
public class SecretSantaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecretSantaApplication.class, args);
    }
}
