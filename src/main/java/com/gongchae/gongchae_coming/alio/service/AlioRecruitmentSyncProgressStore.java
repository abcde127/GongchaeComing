package com.gongchae.gongchae_coming.alio.service;

import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentSyncProgressResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AlioRecruitmentSyncProgressStore {

	private final Map<String, AlioRecruitmentSyncProgressResponse> progressBySessionId = new ConcurrentHashMap<>();

	public void start(String sessionId) {
		progressBySessionId.put(sessionId, new AlioRecruitmentSyncProgressResponse(true, 0, 0, 0, 0, 0));
	}

	public void update(String sessionId, int currentPage, int totalPages, int fetchedCount, int totalCount) {
		int percentage = totalPages > 0
			? Math.min(100, Math.round((currentPage * 100.0f) / totalPages))
			: 0;
		progressBySessionId.put(
			sessionId,
			new AlioRecruitmentSyncProgressResponse(
				true,
				currentPage,
				totalPages,
				fetchedCount,
				totalCount,
				percentage
			)
		);
	}

	public void complete(String sessionId, int currentPage, int totalPages, int fetchedCount, int totalCount) {
		progressBySessionId.put(
			sessionId,
			new AlioRecruitmentSyncProgressResponse(
				false,
				currentPage,
				totalPages,
				fetchedCount,
				totalCount,
				totalPages > 0 ? 100 : 0
			)
		);
	}

	public AlioRecruitmentSyncProgressResponse get(String sessionId) {
		return progressBySessionId.getOrDefault(
			sessionId,
			new AlioRecruitmentSyncProgressResponse(false, 0, 0, 0, 0, 0)
		);
	}
}
