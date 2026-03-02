package com.revshop;

import com.revshop.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class RevShopApplication {

	static {
		AppConfig.loadDotenv();
	}

	public static void main(String[] args) {
		SpringApplication.run(RevShopApplication.class, args);
	}
}
