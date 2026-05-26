package com.gongchae.gongchae_coming.alio.dto;

import com.gongchae.gongchae_coming.alio.domain.PublicInstitution;

public record PublicInstitutionOptionResponse(
	String detailCode,
	String detailName
) {

	public static PublicInstitutionOptionResponse from(PublicInstitution institution) {
		return new PublicInstitutionOptionResponse(institution.getInstCd(), institution.getInstNm());
	}
}
