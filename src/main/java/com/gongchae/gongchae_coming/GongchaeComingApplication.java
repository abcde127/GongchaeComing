package com.gongchae.gongchae_coming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class GongchaeComingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GongchaeComingApplication.class, args);
	}

}
