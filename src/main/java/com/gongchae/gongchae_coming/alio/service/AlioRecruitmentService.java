package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlioRecruitmentService {

	private final AlioRecruitmentClient alioRecruitmentClient;

	public JsonNode getRecruitments(AlioRecruitmentListRequest request) {
		return alioRecruitmentClient.fetchRecruitments(request);
	}
}
