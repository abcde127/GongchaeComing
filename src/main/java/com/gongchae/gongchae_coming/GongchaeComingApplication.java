package com.gongchae.gongchae_coming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@EnableScheduling
@SpringBootApplication
public class GongchaeComingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GongchaeComingApplication.class, args);
	}

}
