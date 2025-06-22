package com.juanfridano.cihealthchecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CihealthcheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CihealthcheckerApplication.class, args);
	}

}
