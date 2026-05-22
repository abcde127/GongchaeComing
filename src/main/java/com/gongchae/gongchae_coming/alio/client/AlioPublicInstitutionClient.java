package com.gongchae.gongchae_coming.alio.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.config.AlioApiProperties;
import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AlioPublicInstitutionClient {

	private static final String REQUEST_METHOD = "GET";
	private final RestClient restClient;
	private final AlioApiProperties properties;

	public JsonNode fetchPublicInstitutions(int pageNo, int numOfRows) {
		validateConfiguration();
		URI uri = buildUri(pageNo, numOfRows);

		try {
			return restClient.get()
				.uri(uri)
				.retrieve()
				.body(JsonNode.class);
		} catch (Exception exception) {
			throw buildAlioApiException(uri, exception);
		}
	}

	private URI buildUri(int pageNo, int numOfRows) {
		String uri = UriComponentsBuilder
			.fromUriString(properties.publicInstListUrl())
			.queryParam("pageNo", pageNo)
			.queryParam("numOfRows", numOfRows)
			.build()
			.toUri()
			.toString();
		return URI.create(uri + "&serviceKey=" + properties.serviceKey());
	}

	private void validateConfiguration() {
		if (!StringUtils.hasText(properties.publicInstListUrl())) {
			throw new AlioApiException("ALIO public institution list URL is not configured.");
		}

		if (!properties.hasServiceKey()) {
			throw new AlioApiException("ALIO API serviceKey is not configured.");
		}
	}

	private AlioApiException buildAlioApiException(URI requestUri, Exception exception) {
		StringBuilder message = new StringBuilder("Failed to fetch public institution list from ALIO API");
		String requestUriText = requestUri.toString();

		if (exception instanceof RestClientResponseException responseException) {
			message.append(" (status: ").append(responseException.getStatusCode()).append(")");
			message.append(". Request method: ").append(REQUEST_METHOD);
			message.append(". Request URI: ").append(requestUriText);

			String responseBody = responseException.getResponseBodyAsString();
			if (StringUtils.hasText(responseBody)) {
				message.append(". Response body: ").append(responseBody);
			}

			return new AlioApiException(
				message.toString(),
				exception,
				responseException.getStatusCode(),
				responseBody,
				REQUEST_METHOD,
				requestUriText
			);
		}

		message.append(". Request method: ").append(REQUEST_METHOD);
		message.append(". Request URI: ").append(requestUriText);

		if (StringUtils.hasText(exception.getMessage())) {
			message.append(". Cause: ").append(exception.getMessage());
		}

		return new AlioApiException(message.toString(), exception, null, null, REQUEST_METHOD, requestUriText);
	}
}
