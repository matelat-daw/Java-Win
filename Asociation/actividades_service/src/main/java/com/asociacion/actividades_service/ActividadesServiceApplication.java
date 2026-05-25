package com.asociacion.actividades_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ActividadesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActividadesServiceApplication.class, args);
	}
}