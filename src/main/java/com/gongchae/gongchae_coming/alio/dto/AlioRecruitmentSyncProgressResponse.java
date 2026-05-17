package com.gongchae.gongchae_coming.alio.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record AlioRecruitmentSyncProgressResponse(
	boolean inProgress,
	int currentPage,
	int totalPages,
	int fetchedCount,
	int totalCount,
	int percentage,
	String status,
	String message,
	int failedPage,
	JsonNode failureResponse
) {
}
