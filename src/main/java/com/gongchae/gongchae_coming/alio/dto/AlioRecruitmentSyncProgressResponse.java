package com.gongchae.gongchae_coming.alio.dto;

public record AlioRecruitmentSyncProgressResponse(
	boolean inProgress,
	int currentPage,
	int totalPages,
	int fetchedCount,
	int totalCount,
	int percentage
) {
}
