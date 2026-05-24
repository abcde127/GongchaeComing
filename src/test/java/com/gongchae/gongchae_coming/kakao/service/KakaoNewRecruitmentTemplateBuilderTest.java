package com.gongchae.gongchae_coming.kakao.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class KakaoNewRecruitmentTemplateBuilderTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final KakaoNewRecruitmentTemplateBuilder templateBuilder =
		new KakaoNewRecruitmentTemplateBuilder("https://gongchae.example.com/");

	@Test
	void buildTemplateObjectCreatesNewRecruitmentTextTemplate() {
		KakaoTemplateObject templateObject = templateBuilder.buildTemplateObject(List.of(
			recruitment("한국전력공사 신입 채용", "20260610"),
			recruitment("국민건강보험공단 인턴", "20260620"),
			recruitment("서울교통공사", "20260630")
		));

		JsonNode template = OBJECT_MAPPER.valueToTree(templateObject);

		assertThat(template.path("object_type").asText()).isEqualTo("text");
		assertThat(template.path("text").asText())
			.isEqualTo("[공채왔어요] 신규공고 알림 🔔\n"
				+ "맞춤공고로 설정한 조건에 맞는 신규 채용공고 3건이 등록됐어요!\n"
				+ "\n"
				+ "💌 한국전력공사 신입… (~ 2026.06.10)\n"
				+ "💌 국민건강보험공단… (~ 2026.06.20)\n"
				+ "\n"
				+ "외 1개의 공고를 더 확인해보세요 👇");
		assertThat(template.path("link").path("web_url").asText()).isEqualTo("https://gongchae.example.com/recruitments");
		assertThat(template.path("buttons").get(0).path("title").asText()).isEqualTo("공고 확인하기");
	}

	private AlioRecruitment recruitment(String title, String deadlineDate) {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		item.put("recrutPblntSn", Math.abs(title.hashCode()));
		item.put("recrutPbancTtl", title);
		item.put("pbancBgngYmd", "20260501");
		item.put("pbancEndYmd", deadlineDate);
		return AlioRecruitment.from(item, LocalDateTime.now());
	}
}
