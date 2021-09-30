package com.bad.studios.tellerbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCaching
public class TellerbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TellerbotApplication.class, args);
	}

}
