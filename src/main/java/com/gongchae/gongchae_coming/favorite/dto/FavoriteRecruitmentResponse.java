package com.gongchae.gongchae_coming.favorite.dto;

import com.gongchae.gongchae_coming.favorite.domain.FavoriteRecruitment;
import java.time.LocalDateTime;

public record FavoriteRecruitmentResponse(
	Long id,
	Long memberId,
	String source,
	String sourceRecruitmentId,
	String recruitmentTitle,
	String institutionName,
	String hireType,
	String workRegion,
	String recruitmentStartDate,
	String recruitmentEndDate,
	String recruitmentUrl,
	LocalDateTime createdAt,
	boolean created
) {

	public static FavoriteRecruitmentResponse from(FavoriteRecruitment favoriteRecruitment, boolean created) {
		return new FavoriteRecruitmentResponse(
			favoriteRecruitment.getId(),
			favoriteRecruitment.getMember().getId(),
			favoriteRecruitment.getSource().name(),
			favoriteRecruitment.getSourceRecruitmentId(),
			favoriteRecruitment.getRecruitmentTitle(),
			favoriteRecruitment.getInstitutionName(),
			favoriteRecruitment.getHireType(),
			favoriteRecruitment.getWorkRegion(),
			favoriteRecruitment.getRecruitmentStartDate(),
			favoriteRecruitment.getRecruitmentEndDate(),
			favoriteRecruitment.getRecruitmentUrl(),
			favoriteRecruitment.getCreatedAt(),
			created
		);
	}
}
