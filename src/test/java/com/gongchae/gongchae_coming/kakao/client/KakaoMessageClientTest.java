package com.gongchae.gongchae_coming.kakao.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gongchae.gongchae_coming.kakao.dto.KakaoButton;
import com.gongchae.gongchae_coming.kakao.dto.KakaoLink;
import com.gongchae.gongchae_coming.kakao.dto.KakaoListContent;
import com.gongchae.gongchae_coming.kakao.dto.KakaoListTemplateObject;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KakaoMessageClientTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void sendDefaultMessageSerializesTemplateObjectAsJsonFormParameter() throws Exception {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KakaoMessageClient client = new KakaoMessageClient(builder.build(), OBJECT_MAPPER);

		server.expect(method(HttpMethod.POST))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
			.andExpect(header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8"))
			.andExpect(request -> {
				String body = ((MockClientHttpRequest) request).getBodyAsString(StandardCharsets.UTF_8);
				assertThat(body).startsWith("template_object=");

				String templateObject = URLDecoder.decode(
					body.substring("template_object=".length()),
					StandardCharsets.UTF_8
				);
				JsonNode json = OBJECT_MAPPER.readTree(templateObject);
				assertThat(json.path("object_type").asText()).isEqualTo("list");
				assertThat(json.path("contents").get(0).path("title").asText()).isEqualTo("한국전력공사 채용");
			})
			.andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		client.sendDefaultMessage("access-token", templateObject());

		server.verify();
	}

	@Test
	void sendDefaultMessageRejectsNullTemplateObject() {
		KakaoMessageClient client = new KakaoMessageClient(RestClient.create(), OBJECT_MAPPER);

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.sendDefaultMessage("access-token", null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("template_object must not be null");
	}

	private KakaoListTemplateObject templateObject() {
		KakaoLink link = new KakaoLink("https://gongchae.example.com/mypage?section=favorites", "https://gongchae.example.com/mypage?section=favorites");
		return new KakaoListTemplateObject(
			"list",
			"[공채왔어요 리마인드] - 총 1개의 관심 공고가 등록되어 있습니다",
			link,
			List.of(new KakaoListContent(
				"한국전력공사 채용",
				"한국전력공사 · 마감까지 7일",
				new KakaoLink("https://alio.example.com/1", "https://alio.example.com/1")
			)),
			List.of(new KakaoButton("관심공고 더보기", link))
		);
	}
}
