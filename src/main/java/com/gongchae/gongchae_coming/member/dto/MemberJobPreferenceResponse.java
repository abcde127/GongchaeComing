package com.gongchae.gongchae_coming.member.dto;

import com.gongchae.gongchae_coming.member.domain.Member;
import java.util.List;

public record MemberJobPreferenceResponse(
	String searchKeyword,
	List<String> companies,
	List<String> recruitmentStatuses,
	List<String> regions,
	List<String> categories,
	List<String> hireTypes,
	List<String> ncsCodes
) {

	public static MemberJobPreferenceResponse from(Member member) {
		return new MemberJobPreferenceResponse(
			member.getPreferredSearchKeyword(),
			member.splitPreferredCompanies(),
			member.splitPreferredRecruitmentStatuses(),
			member.splitPreferredRegions(),
			member.splitPreferredCategories(),
			member.splitPreferredHireTypes(),
			member.splitPreferredNcsCodes()
		);
	}
}
