package com.gongchae.gongchae_coming.alio.controller;

import static com.gongchae.gongchae_coming.alio.controller.RecruitmentViewController.REFRESH_ALIO_RECRUITMENTS_SESSION_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentSyncProgressResponse;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentService;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentSyncProgressStore;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
		HttpSession session
	) {
		boolean shouldRefresh = refresh
			|| Boolean.TRUE.equals(session.getAttribute(REFRESH_ALIO_RECRUITMENTS_SESSION_KEY));
		session.removeAttribute(REFRESH_ALIO_RECRUITMENTS_SESSION_KEY);
		return alioRecruitmentService.getRecruitments(request, shouldRefresh, session.getId());
	}

	@GetMapping("/sync-progress")
	public AlioRecruitmentSyncProgressResponse getSyncProgress(HttpSession session) {
		return syncProgressStore.get(session.getId());
	}
}
