package com.gongchae.gongchae_coming.alio.controller;

import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.dto.RecruitmentRedirectUrlResponse;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruitments")
public class RecruitmentRedirectController {

	private final AlioRecruitmentRepository alioRecruitmentRepository;

	@GetMapping("/{recruitmentId}/redirect")
	public ResponseEntity<Void> redirectToRecruitment(@PathVariable String recruitmentId) {
		URI location = findRecruitmentRedirectUri(recruitmentId);
		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, location.toString())
			.build();
	}

	@GetMapping("/{recruitmentId}/redirect-url")
	public RecruitmentRedirectUrlResponse getRecruitmentRedirectUrl(@PathVariable String recruitmentId) {
		return new RecruitmentRedirectUrlResponse(findRecruitmentRedirectUri(recruitmentId).toString());
	}

	private URI findRecruitmentRedirectUri(String recruitmentId) {
		Long recruitmentSequence = parseRecruitmentSequence(recruitmentId);
		return alioRecruitmentRepository.findByRecrutPblntSn(recruitmentSequence)
			.map(AlioRecruitment::getRecruitmentUrl)
			.map(this::resolveRedirectUri)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "recruitment link not found"));
	}

	private Long parseRecruitmentSequence(String recruitmentId) {
		try {
			return Long.parseLong(recruitmentId);
		} catch (NumberFormatException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "recruitment link not found");
		}
	}

	private URI resolveRedirectUri(String recruitmentUrl) {
		if (!StringUtils.hasText(recruitmentUrl)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "recruitment link not found");
		}

		URI uri = URI.create(recruitmentUrl.trim());
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid recruitment link");
		}
		return uri;
	}
}
