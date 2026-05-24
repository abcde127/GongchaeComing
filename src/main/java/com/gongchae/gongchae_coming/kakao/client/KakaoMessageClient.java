package com.gongchae.gongchae_coming.kakao.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMessageClient {

	private static final String KAKAO_DEFAULT_MESSAGE_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";
	private static final MediaType FORM_URLENCODED_UTF8 = new MediaType(
		MediaType.APPLICATION_FORM_URLENCODED,
		StandardCharsets.UTF_8
	);

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public void sendDefaultMessage(String accessToken, KakaoTemplateObject templateObject) {
		validateAccessToken(accessToken);
		String templateObjectJson = serializeTemplateObject(templateObject);
		validateTemplateObjectJson(templateObjectJson);
		log.info("Sending Kakao template_object={}", templateObjectJson);

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("template_object", templateObjectJson);

		restClient.post()
			.uri(KAKAO_DEFAULT_MESSAGE_URL)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.contentType(FORM_URLENCODED_UTF8)
			.accept(MediaType.APPLICATION_JSON)
			.body(formData)
			.retrieve()
			.toBodilessEntity();
	}

	private String serializeTemplateObject(KakaoTemplateObject templateObject) {
		if (templateObject == null) {
			throw new IllegalArgumentException("template_object must not be null");
		}

		try {
			return objectMapper.writeValueAsString(templateObject);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("failed to serialize template_object", exception);
		}
	}

	private void validateTemplateObjectJson(String templateObjectJson) {
		if (!StringUtils.hasText(templateObjectJson) || "-".equals(templateObjectJson.trim())) {
			throw new IllegalArgumentException("template_object must not be blank or '-'");
		}
		if (!templateObjectJson.trim().startsWith("{")) {
			throw new IllegalArgumentException("template_object must be serialized as a JSON object");
		}
		try {
			validateTemplateObjectNode(objectMapper.readTree(templateObjectJson));
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("template_object must be valid JSON", exception);
		}
	}

	private void validateAccessToken(String accessToken) {
		if (!StringUtils.hasText(accessToken) || "-".equals(accessToken.trim())) {
			throw new IllegalArgumentException("Kakao access token must not be blank or '-'");
		}
	}

	private void validateTemplateObjectNode(JsonNode node) {
		if (node == null || node.isNull()) {
			throw new IllegalArgumentException("template_object must not contain null");
		}
		if (node.isTextual() && "-".equals(node.asText().trim())) {
			throw new IllegalArgumentException("template_object must not contain '-'");
		}
		if (node.isContainerNode()) {
			node.forEach(this::validateTemplateObjectNode);
		}
	}
}
