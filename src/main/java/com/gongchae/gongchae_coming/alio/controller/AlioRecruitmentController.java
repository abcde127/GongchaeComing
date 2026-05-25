package com.gongchae.gongchae_coming.alio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentStatisticsResponse;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentSyncProgressResponse;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentService;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentSyncProgressStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruitments/alio")
public class AlioRecruitmentController {

	private final AlioRecruitmentService alioRecruitmentService;
	private final AlioRecruitmentSyncProgressStore syncProgressStore;

	@GetMapping
	public JsonNode getRecruitmentList(@Valid @ModelAttribute AlioRecruitmentListRequest request) {
		return alioRecruitmentService.getRecruitments(request);
	}

	@GetMapping("/statistics")
	public AlioRecruitmentStatisticsResponse getRecruitmentStatistics() {
		return alioRecruitmentService.getRecruitmentStatistics();
	}

	@GetMapping("/statistics/summary")
	public AlioRecruitmentStatisticsResponse.Summary getRecruitmentStatisticsSummary() {
		return alioRecruitmentService.getRecruitmentStatisticsSummary();
	}

	@GetMapping("/statistics/monthly-start-counts")
	public java.util.List<AlioRecruitmentStatisticsResponse.MonthlyCount> getRecruitmentMonthlyStartCounts() {
		return alioRecruitmentService.getRecruitmentMonthlyStartCounts();
	}

	@GetMapping("/statistics/region-counts")
	public java.util.List<AlioRecruitmentStatisticsResponse.RegionCount> getRecruitmentRegionCounts() {
		return alioRecruitmentService.getRecruitmentRegionCounts();
	}

	@PostMapping("/sync")
	public AlioRecruitmentSyncProgressResponse startSynchronization() {
		alioRecruitmentService.startBackgroundSynchronization(emptyRequest());
		return syncProgressStore.get();
	}

	@GetMapping("/sync-status")
	public AlioRecruitmentSyncProgressResponse getSynchronizationStatus() {
		return syncProgressStore.get();
	}

	@GetMapping(path = "/sync-events", produces = "text/event-stream")
	public SseEmitter streamSyncProgress() {
		return syncProgressStore.subscribe();
	}

	private AlioRecruitmentListRequest emptyRequest() {
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
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}
}
