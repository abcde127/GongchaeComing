package com.gongchae.gongchae_coming.alio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentSyncProgressResponse;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentService;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentSyncProgressStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruitments/alio")
public class AlioRecruitmentController {

	private final AlioRecruitmentService alioRecruitmentService;
	private final AlioRecruitmentSyncProgressStore syncProgressStore;

	@GetMapping
	public JsonNode getRecruitmentList(
		@Valid @ModelAttribute AlioRecruitmentListRequest request,
		@RequestParam(defaultValue = "false") boolean refresh,
		@RequestParam(defaultValue = "false") boolean resume
	) {
		return alioRecruitmentService.getRecruitments(request, refresh, resume, null);
	}

	@GetMapping("/sync-progress")
	public AlioRecruitmentSyncProgressResponse getSyncProgress() {
		return syncProgressStore.get();
	}

	@PostMapping("/sync-cancel")
	public AlioRecruitmentSyncProgressResponse cancelSync() {
		alioRecruitmentService.cancelBackgroundSynchronization();
		return syncProgressStore.get();
	}

	@PostMapping("/sync-pause")
	public AlioRecruitmentSyncProgressResponse pauseSync() {
		alioRecruitmentService.pauseBackgroundSynchronization();
		return syncProgressStore.get();
	}

	@PostMapping("/sync-resume")
	public AlioRecruitmentSyncProgressResponse resumeSync() {
		alioRecruitmentService.resumePausedSynchronization();
		return syncProgressStore.get();
	}
}
