package com.gongchae.gongchae_coming.alio.service;

import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlioRecruitmentStartupSyncRunner {

	private final AlioRecruitmentService alioRecruitmentService;
	private final AlioRecruitmentSeedImporter alioRecruitmentSeedImporter;
	private final PublicInstitutionService publicInstitutionService;

	@EventListener(ApplicationReadyEvent.class)
	public void startInitialSync() {
		try {
			alioRecruitmentSeedImporter.importSeedRecruitments();
		} catch (Exception exception) {
			log.warn("Failed to import ALIO recruitment seed data on startup.", exception);
		}
		try {
			publicInstitutionService.synchronizePublicInstitutions();
		} catch (Exception exception) {
			log.warn("Failed to synchronize public institutions on startup.", exception);
		}
		alioRecruitmentService.startBackgroundSynchronization(emptyRequest());
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
