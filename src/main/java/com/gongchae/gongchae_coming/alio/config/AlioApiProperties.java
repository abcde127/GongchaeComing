package com.gongchae.gongchae_coming.alio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.alio")
public record AlioApiProperties(
	String baseUrl,
	String recruitListPath,
	String serviceKey,
	String resultType
) {

	public String resolvedResultType() {
		return (resultType == null || resultType.isBlank()) ? "json" : resultType;
	}

	public boolean hasServiceKey() {
		return serviceKey != null && !serviceKey.isBlank();
	}
}
