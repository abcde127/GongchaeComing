package com.gongchae.gongchae_coming.alio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.dto.AlioFilterOptionResponse;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruitments/alio")
public class AlioRecruitmentController {

	private final AlioRecruitmentService alioRecruitmentService;

	@GetMapping
	public JsonNode getRecruitmentList(@Valid @ModelAttribute AlioRecruitmentListRequest request) {
		return alioRecruitmentService.getRecruitments(request);
	}

	@GetMapping("/filters/ncs")
	public List<AlioFilterOptionResponse> getNcsFilterOptions() {
		return alioRecruitmentService.getNcsFilterOptions();
	}

	@GetMapping("/filters/work-regions")
	public List<AlioFilterOptionResponse> getWorkRegionFilterOptions() {
		return alioRecruitmentService.getWorkRegionFilterOptions();
	}
}
