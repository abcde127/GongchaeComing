package com.gongchae.gongchae_coming.alio.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlioRecruitmentListRequestTest {

	@Test
	void resolvedRecruitmentTitleKeywordUsesSearchKeywordWhenTitleIsMissing() {
		var request = request("  nhis  ", null, null);

		assertThat(request.resolvedRecruitmentTitleKeyword()).isEqualTo("nhis");
	}

	@Test
	void resolvedRecruitmentTitleKeywordPrefersExplicitTitleFilter() {
		var request = request("nhis", "  nhis recruitment  ", null);

		assertThat(request.resolvedRecruitmentTitleKeyword()).isEqualTo("nhis recruitment");
	}

	@Test
	void resolvedRecruitmentTitleKeywordReturnsNullWhenBothInputsAreBlank() {
		var request = request(" ", " ", null);

		assertThat(request.resolvedRecruitmentTitleKeyword()).isNull();
	}

	@Test
	void resolvedSortByDefaultsToRegistrationDate() {
		var request = request("nhis", null, null);

		assertThat(request.resolvedSortBy()).isEqualTo("REGISTRATION_DATE");
	}

	@Test
	void resolvedSortByReturnsExplicitSortType() {
		var request = request("nhis", null, "DEADLINE_DATE");

		assertThat(request.resolvedSortBy()).isEqualTo("DEADLINE_DATE");
	}

	private AlioRecruitmentListRequest request(String searchKeyword, String recrutPbancTtl, String sortBy) {
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
			sortBy,
			null
		);
	}
}
