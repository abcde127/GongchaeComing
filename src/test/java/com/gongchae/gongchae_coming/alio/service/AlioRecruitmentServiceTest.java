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
}
