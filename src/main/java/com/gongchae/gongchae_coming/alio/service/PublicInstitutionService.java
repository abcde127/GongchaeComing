package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.client.AlioPublicInstitutionClient;
import com.gongchae.gongchae_coming.alio.domain.PublicInstitution;
import com.gongchae.gongchae_coming.alio.repository.PublicInstitutionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PublicInstitutionService {

	private static final int SYNC_PAGE_SIZE = 400;

	private final AlioPublicInstitutionClient alioPublicInstitutionClient;
	private final PublicInstitutionRepository publicInstitutionRepository;

	@Transactional
	public int synchronizePublicInstitutions() {
		JsonNode response = alioPublicInstitutionClient.fetchPublicInstitutions(1, SYNC_PAGE_SIZE);
		List<PublicInstitution> institutions = new ArrayList<>(extractInstitutions(response));
		int totalCount = response.path("totalCount").asInt(institutions.size());
		int totalPages = (int) Math.ceil((double) totalCount / SYNC_PAGE_SIZE);

		for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
			institutions.addAll(extractInstitutions(alioPublicInstitutionClient.fetchPublicInstitutions(pageNo, SYNC_PAGE_SIZE)));
		}

		publicInstitutionRepository.saveAll(institutions);
		return institutions.size();
	}

	private List<PublicInstitution> extractInstitutions(JsonNode response) {
		JsonNode results = response.path("result");
		if (!results.isArray()) {
			throw new IllegalStateException("public institution response result must be an array");
		}

		LocalDateTime fetchedAt = LocalDateTime.now();
		List<PublicInstitution> institutions = new ArrayList<>();
		results.forEach(item -> {
			if (StringUtils.hasText(item.path("instCd").asText(null)) && StringUtils.hasText(item.path("instNm").asText(null))) {
				institutions.add(PublicInstitution.from(item, fetchedAt));
			}
		});

		return institutions;
	}
}
