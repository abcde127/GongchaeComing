package com.gongchae.gongchae_coming.member.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record MemberJobPreferenceRequest(
	@Size(max = 100, message = "searchKeyword must be 100 characters or less")
	String searchKeyword,
	List<String> companies,
	List<String> recruitmentStatuses,
	List<String> regions,
	List<String> categories,
	List<String> hireTypes,
	List<String> ncsCodes
) {
}
