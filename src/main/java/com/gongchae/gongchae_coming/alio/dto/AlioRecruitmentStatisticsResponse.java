package com.gongchae.gongchae_coming.alio.dto;

import java.util.List;

public record AlioRecruitmentStatisticsResponse(
	long totalCount,
	String lastFetchedAt,
	List<StatusCount> statusCounts,
	List<MonthlyCount> monthlyStartCounts,
	List<RegionCount> regionCounts
) {

	public record Summary(
		long totalCount,
		long scheduledCount,
		long activeCount,
		String referenceAt
	) {
	}

	public record StatusCount(
		String status,
		String label,
		long count
	) {
	}

	public record MonthlyCount(
		String yearMonth,
		long count
	) {
	}

	public record YearlyCount(
		String year,
		long count
	) {
	}

	public record CategoryCount(
		String code,
		String label,
		long count
	) {
	}

	public record RegionCount(
		String code,
		String label,
		long count
	) {
	}
}
