package com.gongchae.gongchae_coming.alio.scheduler;

import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.service.AlioRecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlioRecruitmentSyncScheduler {

	private final AlioRecruitmentService alioRecruitmentService;

	@Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT10M")
	public void synchronizeRecruitments() {
		try {
			alioRecruitmentService.startBackgroundSynchronization(emptyRequest());
		} catch (Exception exception) {
			log.warn("Failed to schedule ALIO recruitment synchronization.", exception);
		}
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
