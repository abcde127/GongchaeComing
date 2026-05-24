package com.gongchae.gongchae_coming.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoTextTemplateObject(
	@JsonProperty("object_type")
	String objectType,
	String text,
	KakaoLink link,
	List<KakaoButton> buttons
) implements KakaoTemplateObject {
}
