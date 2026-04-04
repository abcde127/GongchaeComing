package com.gongchae.gongchae_coming.alio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AlioClientConfig {

	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}
}
