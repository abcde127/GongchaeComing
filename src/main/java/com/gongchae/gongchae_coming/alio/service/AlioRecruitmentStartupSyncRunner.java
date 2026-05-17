package com.gongchae.gongchae_coming.alio.service;

import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlioRecruitmentStartupSyncRunner {

	private final AlioRecruitmentService alioRecruitmentService;

	@EventListener(ApplicationReadyEvent.class)
	public void startInitialSync() {
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
