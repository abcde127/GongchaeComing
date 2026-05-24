package com.gongchae.gongchae_coming.alio.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RecruitmentRedirectControllerTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private RecruitmentRedirectController controller;

	@Autowired
	private AlioRecruitmentRepository alioRecruitmentRepository;

	@Test
	void redirectToRecruitmentReturnsFoundLocation() {
		saveRecruitment();

		var response = controller.redirectToRecruitment("300658");

		assertThat(response.getStatusCode().value()).isEqualTo(302);
		assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION))
			.isEqualTo("https://alio.example.com/recruitments/1");
	}

	@Test
	void getRecruitmentRedirectUrlReturnsOriginalUrl() {
		saveRecruitment();

		var response = controller.getRecruitmentRedirectUrl("300658");

		assertThat(response.url()).isEqualTo("https://alio.example.com/recruitments/1");
	}

	private void saveRecruitment() {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		item.put("recrutPblntSn", 300658);
		item.put("recrutPbancTtl", "공고");
		item.put("pblntInstNm", "기관");
		item.put("recrutPbancUrl", "https://alio.example.com/recruitments/1");
		alioRecruitmentRepository.save(AlioRecruitment.from(item, LocalDateTime.now()));
	}
}
