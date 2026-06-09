package com.asociaciondomitila;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DomitilaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomitilaApplication.class, args);
    }
}