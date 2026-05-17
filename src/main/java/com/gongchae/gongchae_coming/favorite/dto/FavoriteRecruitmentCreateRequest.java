package com.gongchae.gongchae_coming.favorite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FavoriteRecruitmentCreateRequest(
	@Size(max = 20, message = "source must be 20 characters or fewer")
	String source,
	@NotBlank(message = "sourceRecruitmentId is required")
	@Size(max = 100, message = "sourceRecruitmentId must be 100 characters or fewer")
	String sourceRecruitmentId,
	@NotBlank(message = "recruitmentTitle is required")
	@Size(max = 255, message = "recruitmentTitle must be 255 characters or fewer")
	String recruitmentTitle,
	@NotBlank(message = "institutionName is required")
	@Size(max = 100, message = "institutionName must be 100 characters or fewer")
	String institutionName,
	@Size(max = 100, message = "hireType must be 100 characters or fewer")
	String hireType,
	@Size(max = 100, message = "workRegion must be 100 characters or fewer")
	String workRegion,
	@Size(max = 30, message = "recruitmentStartDate must be 30 characters or fewer")
	String recruitmentStartDate,
	@Size(max = 30, message = "recruitmentEndDate must be 30 characters or fewer")
	String recruitmentEndDate,
	@Size(max = 500, message = "recruitmentUrl must be 500 characters or fewer")
	String recruitmentUrl
) {
}
