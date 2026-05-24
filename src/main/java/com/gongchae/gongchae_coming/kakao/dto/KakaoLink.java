package com.gongchae.gongchae_coming.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoLink(
	@JsonProperty("web_url")
	String webUrl,
	@JsonProperty("mobile_web_url")
	String mobileWebUrl
) {
}
