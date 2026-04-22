package com.gongchae.gongchae_coming.alio.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlioRecruitmentListRequestTest {

	@Test
	void resolvedRecruitmentTitleKeywordUsesSearchKeywordWhenTitleIsMissing() {
		var request = request("  nhis  ", null);

		assertThat(request.resolvedRecruitmentTitleKeyword()).isEqualTo("nhis");
	}

	@Test
	void resolvedRecruitmentTitleKeywordPrefersExplicitTitleFilter() {
		var request = request("nhis", "  nhis recruitment  ");

		assertThat(request.resolvedRecruitmentTitleKeyword()).isEqualTo("nhis recruitment");
	}

	@Test
	void resolvedRecruitmentTitleKeywordReturnsNullWhenBothInputsAreBlank() {
		var request = request(" ", " ");

		assertThat(request.resolvedRecruitmentTitleKeyword()).isNull();
	}

	private AlioRecruitmentListRequest request(String searchKeyword, String recrutPbancTtl) {
		return new AlioRecruitmentListRequest(
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			searchKeyword,
			recrutPbancTtl,
			null,
			null,
			null,
			null
		);
	}
}
