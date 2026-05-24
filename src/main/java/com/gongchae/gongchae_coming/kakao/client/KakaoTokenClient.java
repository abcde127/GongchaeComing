package com.gongchae.gongchae_coming.kakao.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoTokenClient {

	private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

	private final RestClient restClient;

	@Value("${KAKAO_API_KEY:}")
	private String kakaoApiKey;

	@Value("${KAKAO_CLIENT_SECRET_KEY:}")
	private String kakaoClientSecretKey;

	public KakaoRefreshTokenResponse refreshAccessToken(String refreshToken) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", "refresh_token");
		formData.add("client_id", requireKakaoApiKey());
		formData.add("refresh_token", refreshToken);
		if (StringUtils.hasText(kakaoClientSecretKey)) {
			formData.add("client_secret", kakaoClientSecretKey.trim());
		}

		return restClient.post()
			.uri(KAKAO_TOKEN_URL)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(formData)
			.retrieve()
			.body(KakaoRefreshTokenResponse.class);
	}

	private String requireKakaoApiKey() {
		if (StringUtils.hasText(kakaoApiKey)) {
			return kakaoApiKey.trim();
		}
		throw new IllegalStateException("KAKAO_API_KEY is not configured");
	}

	public record KakaoRefreshTokenResponse(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("expires_in")
		Integer expiresIn,
		@JsonProperty("refresh_token")
		String refreshToken,
		@JsonProperty("refresh_token_expires_in")
		Integer refreshTokenExpiresIn
	) {
	}
}
