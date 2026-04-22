package com.gongchae.gongchae_coming.alio.service;

import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AlioRecruitmentServiceTest {

	private final AlioRecruitmentService alioRecruitmentService =
		new AlioRecruitmentService(mock(AlioRecruitmentClient.class));

	@Test
	void getNcsFilterOptionsReturnsAlioNcsCodes() {
		var options = alioRecruitmentService.getNcsFilterOptions();

		assertThat(options).hasSize(25);
		assertThat(options.get(0).code()).isEqualTo("R600001");
		assertThat(options.get(0).name()).isEqualTo("사업관리");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("R600025");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("연구");
	}

	@Test
	void getWorkRegionFilterOptionsReturnsAlioWorkRegionCodes() {
		var options = alioRecruitmentService.getWorkRegionFilterOptions();

		assertThat(options).hasSize(18);
		assertThat(options.get(0).code()).isEqualTo("R3010");
		assertThat(options.get(0).name()).isEqualTo("서울특별시");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("R3030");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("해외");
	}

	@Test
	void getHireTypeFilterOptionsReturnsAlioHireTypeCodes() {
		var options = alioRecruitmentService.getHireTypeFilterOptions();

		assertThat(options).hasSize(7);
		assertThat(options.get(0).code()).isEqualTo("R1010");
		assertThat(options.get(0).name()).isEqualTo("정규직");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("R1070");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("청년인턴(채용형)");
	}
	@Test
	void getInstitutionTypeFilterOptionsReturnsInstitutionTypeCodes() {
		var options = alioRecruitmentService.getInstitutionTypeFilterOptions();

		assertThat(options).hasSize(5);
		assertThat(options.get(0).code()).isEqualTo("A2001");
		assertThat(options.get(0).name()).isEqualTo("공기업(시장형)");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("A2005");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("기타공공기관");
	}
}
