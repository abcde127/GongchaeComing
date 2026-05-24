package com.gongchae.gongchae_coming.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoListTemplateObject(
	@JsonProperty("object_type")
	String objectType,
	@JsonProperty("header_title")
	String headerTitle,
	@JsonProperty("header_link")
	KakaoLink headerLink,
	List<KakaoListContent> contents,
	List<KakaoButton> buttons
) implements KakaoTemplateObject {
}
