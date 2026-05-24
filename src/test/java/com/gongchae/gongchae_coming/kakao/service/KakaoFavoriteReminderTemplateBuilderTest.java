package com.gongchae.gongchae_coming.kakao.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentResponse;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class KakaoFavoriteReminderTemplateBuilderTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Clock FIXED_CLOCK = Clock.fixed(
		Instant.parse("2026-05-23T00:00:00Z"),
		ZoneId.of("Asia/Seoul")
	);

	private final KakaoFavoriteReminderTemplateBuilder templateBuilder =
		new KakaoFavoriteReminderTemplateBuilder("https://gongchae.example.com/", FIXED_CLOCK);

	@Test
	void buildTemplateObjectCreatesKakaoListTemplate() throws Exception {
		KakaoTemplateObject templateObject = templateBuilder.buildTemplateObject(List.of(
			favorite("한국전력공사 채용", "한국전력공사", "20260530", "https://alio.example.com/1"),
			favorite("국민건강보험공단 채용", "국민건강보험공단", "2026-05-23", "https://alio.example.com/2"),
			favorite("서울교통공사 채용", "서울교통공사", "2026-05-20", "https://alio.example.com/3"),
			favorite("한국도로공사 채용", "한국도로공사", "2026-06-01", "https://alio.example.com/4")
		));

		JsonNode template = OBJECT_MAPPER.valueToTree(templateObject);

		assertThat(template.path("object_type").asText()).isEqualTo("list");
		assertThat(template.path("header_title").asText())
			.isEqualTo("[공채왔어요 리마인드] - 총 4개의 관심 공고가 등록되어 있습니다");
		assertThat(template.path("header_link").path("web_url").asText())
			.isEqualTo("https://gongchae.example.com/mypage?section=favorites");

		JsonNode contents = template.path("contents");
		assertThat(contents).hasSize(3);
		assertThat(contents.get(0).path("title").asText()).isEqualTo("서울교통공사 채용");
		assertThat(contents.get(0).path("description").asText()).isEqualTo("서울교통공사 · 마감까지 -3일");
		assertThat(contents.get(0).path("link").path("web_url").asText())
			.isEqualTo("https://gongchae.example.com/recruitments/%EC%84%9C%EC%9A%B8%EA%B5%90%ED%86%B5%EA%B3%B5%EC%82%AC%20%EC%B1%84%EC%9A%A9/redirect");
		assertThat(contents.get(1).path("description").asText()).isEqualTo("국민건강보험공단 · 마감까지 0일");
		assertThat(contents.get(2).path("description").asText()).isEqualTo("한국전력공사 · 마감까지 7일");

		JsonNode button = template.path("buttons").get(0);
		assertThat(button.path("title").asText()).isEqualTo("관심공고 더보기");
		assertThat(button.path("link").path("web_url").asText())
			.isEqualTo("https://gongchae.example.com/mypage?section=favorites");
	}

	@Test
	void buildTemplateObjectCreatesTextTemplateWhenSingleLinkableFavorite() throws Exception {
		KakaoTemplateObject templateObject = templateBuilder.buildTemplateObject(List.of(
			favorite("한국전력공사 채용", "한국전력공사", "2026-05-30", "https://alio.example.com/1")
		));

		JsonNode template = OBJECT_MAPPER.valueToTree(templateObject);

		assertThat(template.path("object_type").asText()).isEqualTo("text");
		assertThat(template.path("text").asText())
			.isEqualTo("[공채왔어요 리마인드]\n"
				+ "💙 관심 공고를 확인해주세요 💙\n"
				+ "\n"
				+ "📌 한국전력공사 채용\n"
				+ "🏢 한국전력공사\n"
				+ "⏰ 마감까지 7일 남았어요!\n"
				+ "\n"
				+ "놓치기 전에 지금 바로 확인해보세요!");
		assertThat(template.path("link").path("web_url").asText())
			.isEqualTo("https://gongchae.example.com/recruitments/%ED%95%9C%EA%B5%AD%EC%A0%84%EB%A0%A5%EA%B3%B5%EC%82%AC%20%EC%B1%84%EC%9A%A9/redirect");
		assertThat(template.path("buttons").get(0).path("title").asText()).isEqualTo("원문보기");
		assertThat(template.path("buttons").get(0).path("link").path("web_url").asText())
			.isEqualTo("https://gongchae.example.com/recruitments/%ED%95%9C%EA%B5%AD%EC%A0%84%EB%A0%A5%EA%B3%B5%EC%82%AC%20%EC%B1%84%EC%9A%A9/redirect");
	}

	@Test
	void buildTemplateObjectCreatesTextTemplateWhenFavoriteIsEmpty() {
		KakaoTemplateObject templateObject = templateBuilder.buildTemplateObject(List.of(), LocalTime.of(18, 30));

		JsonNode template = OBJECT_MAPPER.valueToTree(templateObject);

		assertThat(template.path("object_type").asText()).isEqualTo("text");
		assertThat(template.path("text").asText())
			.isEqualTo("[공채왔어요 리마인드]\n"
				+ "\n"
				+ "아직 관심 공고를 등록하지 않았어요..🥺\n"
				+ "\n"
				+ "관심 공고를 등록하면 매일 18시 30분마다 잊지 않게 알려드릴게요!");
		assertThat(template.path("link").path("web_url").asText()).isEqualTo("https://gongchae.example.com/recruitments");
		assertThat(template.path("buttons").get(0).path("title").asText()).isEqualTo("관심공고 설정하러가기");
		assertThat(template.path("buttons").get(0).path("link").path("web_url").asText())
			.isEqualTo("https://gongchae.example.com/recruitments");
	}

	private FavoriteRecruitmentResponse favorite(
		String recruitmentTitle,
		String institutionName,
		String recruitmentEndDate,
		String recruitmentUrl
	) {
		return new FavoriteRecruitmentResponse(
			1L,
			1L,
			"ALIO",
			recruitmentTitle,
			recruitmentTitle,
			institutionName,
			"정규직",
			"서울",
			"2026-05-01",
			recruitmentEndDate,
			recruitmentUrl,
			LocalDateTime.of(2026, 5, 1, 0, 0),
			false
		);
	}
}
