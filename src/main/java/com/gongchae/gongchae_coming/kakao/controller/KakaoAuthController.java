package com.gongchae.gongchae_coming.kakao.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class KakaoAuthController {

	private static final String KAKAO_AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
	private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
	private static final String KAKAO_CALLBACK_PATH = "/api/kakao/oauth/callback";

	@Value("${KAKAO_API_KEY:}")
	private String kakaoApiKey;

	@Value("${KAKAO_CLIENT_SECRET_KEY:}")
	private String kakaoClientSecretKey;

	@Value("${KAKAO_REDIRECT_URI:}")
	private String kakaoRedirectUri;

	private final MemberRepository memberRepository;

	private final RestClient restClient = RestClient.create();

	@GetMapping("/authorize")
	public ResponseEntity<Void> authorize(HttpServletRequest request) {
		String clientId = requireKakaoApiKey();
		String redirectUri = resolveRedirectUri(request);
		URI location = UriComponentsBuilder.fromUriString(KAKAO_AUTHORIZE_URL)
			.queryParam("client_id", clientId)
			.queryParam("redirect_uri", redirectUri)
			.queryParam("response_type", "code")
			.queryParam("scope", "talk_message")
			.build()
			.encode()
			.toUri();

		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, location.toString())
			.build();
	}

	@GetMapping("/oauth/callback")
	@Transactional
	public RedirectView callback(
		HttpServletRequest request,
		Authentication authentication,
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String error,
		@RequestParam(name = "error_description", required = false) String errorDescription
	) {
		if (StringUtils.hasText(error)) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				StringUtils.hasText(errorDescription) ? errorDescription : error
			);
		}
		if (!StringUtils.hasText(code)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kakao authorization code is required");
		}

		KakaoTokenResponse tokenResponse = requestToken(code, resolveRedirectUri(request));
		saveKakaoToken(authentication, tokenResponse);
		return new RedirectView("/mypage?section=notifications&kakao=linked");
	}

	private KakaoTokenResponse requestToken(String code, String redirectUri) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", "authorization_code");
		formData.add("client_id", requireKakaoApiKey());
		formData.add("redirect_uri", redirectUri);
		formData.add("code", code);
		formData.add("client_secret", requireKakaoClientSecretKey());

		return restClient.post()
			.uri(KAKAO_TOKEN_URL)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(formData)
			.retrieve()
			.body(KakaoTokenResponse.class);
	}

	private void saveKakaoToken(Authentication authentication, KakaoTokenResponse tokenResponse) {
		if (
			authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken
		) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login is required");
		}
		if (tokenResponse == null || !tokenResponse.hasRequiredTokenValues()) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "invalid Kakao token response");
		}

		Member member = memberRepository.findByEmail(authentication.getName())
			.orElseThrow(() -> new MemberNotFoundException("member not found"));
		member.updateKakaoToken(
			tokenResponse.accessToken(),
			tokenResponse.expiresIn(),
			tokenResponse.refreshToken(),
			tokenResponse.refreshTokenExpiresIn()
		);
	}

	private String requireKakaoApiKey() {
		if (StringUtils.hasText(kakaoApiKey)) {
			return kakaoApiKey.trim();
		}
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_API_KEY is not configured");
	}

	private String requireKakaoClientSecretKey() {
		if (StringUtils.hasText(kakaoClientSecretKey)) {
			return kakaoClientSecretKey.trim();
		}
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_CLIENT_SECRET_KEY is not configured");
	}

	private String resolveRedirectUri(HttpServletRequest request) {
		if (StringUtils.hasText(kakaoRedirectUri)) {
			return kakaoRedirectUri.trim();
		}

		String scheme = request.getScheme();
		String host = request.getServerName();
		int port = request.getServerPort();
		boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
		String origin = defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
		return origin + KAKAO_CALLBACK_PATH;
	}

	private record KakaoTokenResponse(
		@JsonProperty("token_type")
		String tokenType,
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("id_token")
		String idToken,
		@JsonProperty("expires_in")
		Integer expiresIn,
		@JsonProperty("refresh_token")
		String refreshToken,
		@JsonProperty("refresh_token_expires_in")
		Integer refreshTokenExpiresIn,
		String scope
	) {

		private boolean hasRequiredTokenValues() {
			return StringUtils.hasText(accessToken)
				&& expiresIn != null
				&& StringUtils.hasText(refreshToken)
				&& refreshTokenExpiresIn != null;
		}
	}
}
