package com.gongchae.gongchae_coming.member.dto;

import com.gongchae.gongchae_coming.alio.domain.PublicInstitution;

public record MemberJobPreferenceCompanyResponse(
	String detailCode,
	String detailName
) {

	public static MemberJobPreferenceCompanyResponse from(PublicInstitution institution) {
		return new MemberJobPreferenceCompanyResponse(institution.getInstCd(), institution.getInstNm());
	}
}
