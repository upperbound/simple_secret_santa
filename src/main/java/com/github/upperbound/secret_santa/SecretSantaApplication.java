package com.github.upperbound.secret_santa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SecretSantaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretSantaApplication.class, args);
    }

}
