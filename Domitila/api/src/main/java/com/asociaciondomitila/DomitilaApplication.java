package com.asociaciondomitila;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class DomitilaApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Atlantic/Canary"));
        SpringApplication.run(DomitilaApplication.class, args);
    }
}
