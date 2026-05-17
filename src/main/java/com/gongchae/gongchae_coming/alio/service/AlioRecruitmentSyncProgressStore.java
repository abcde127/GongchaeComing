package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentSyncProgressResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AlioRecruitmentSyncProgressStore {

	public static final String GLOBAL_PROGRESS_KEY = "GLOBAL";

	private final Map<String, AlioRecruitmentSyncProgressResponse> progressByKey = new ConcurrentHashMap<>();

	public void start() {
		start(GLOBAL_PROGRESS_KEY);
	}

	public void start(String sessionId) {
		progressByKey.put(sessionId, new AlioRecruitmentSyncProgressResponse(
			true,
			0,
			0,
			0,
			0,
			0,
			"RUNNING",
			"데이터 갱신을 시작했습니다.",
			0,
			null
		));
	}

	public void update(int currentPage, int totalPages, int fetchedCount, int totalCount) {
		update(GLOBAL_PROGRESS_KEY, currentPage, totalPages, fetchedCount, totalCount);
	}

	public void update(String sessionId, int currentPage, int totalPages, int fetchedCount, int totalCount) {
		int percentage = totalPages > 0
			? Math.min(100, Math.round((currentPage * 100.0f) / totalPages))
			: 0;
		progressByKey.put(
			sessionId,
			new AlioRecruitmentSyncProgressResponse(
				true,
				currentPage,
				totalPages,
				fetchedCount,
				totalCount,
				percentage,
				"RUNNING",
				"데이터 갱신 중입니다.",
				0,
				null
			)
		);
	}

	public void complete(int currentPage, int totalPages, int fetchedCount, int totalCount) {
		complete(GLOBAL_PROGRESS_KEY, currentPage, totalPages, fetchedCount, totalCount);
	}

	public void complete(String sessionId, int currentPage, int totalPages, int fetchedCount, int totalCount) {
		progressByKey.put(
			sessionId,
			new AlioRecruitmentSyncProgressResponse(
				false,
				currentPage,
				totalPages,
				fetchedCount,
				totalCount,
				totalPages > 0 ? 100 : 0,
				"COMPLETED",
				"데이터 갱신이 완료되었습니다.",
				0,
				null
			)
		);
	}

	public void canceling() {
		AlioRecruitmentSyncProgressResponse progress = get();
		progressByKey.put(
			GLOBAL_PROGRESS_KEY,
			new AlioRecruitmentSyncProgressResponse(
				true,
				progress.currentPage(),
				progress.totalPages(),
				progress.fetchedCount(),
				progress.totalCount(),
				progress.percentage(),
				"CANCELING",
				"데이터 갱신 중지를 요청했습니다.",
				0,
				null
			)
		);
	}

	public void canceled(int currentPage, int totalPages, int fetchedCount, int totalCount) {
		int percentage = totalPages > 0
			? Math.min(99, Math.round((currentPage * 100.0f) / totalPages))
			: 0;
		progressByKey.put(
			GLOBAL_PROGRESS_KEY,
			new AlioRecruitmentSyncProgressResponse(
				false,
				currentPage,
				totalPages,
				fetchedCount,
				totalCount,
				percentage,
				"CANCELED",
				"데이터 갱신이 중단되었습니다.",
				0,
				null
			)
		);
	}

	public void fail(
		int currentPage,
		int totalPages,
		int fetchedCount,
		int totalCount,
		String message,
		int failedPage,
		JsonNode failureResponse
	) {
		progressByKey.put(
			GLOBAL_PROGRESS_KEY,
			new AlioRecruitmentSyncProgressResponse(
				false,
				currentPage,
				totalPages,
				fetchedCount,
				totalCount,
				totalPages > 0 ? Math.min(99, Math.round((currentPage * 100.0f) / totalPages)) : 0,
				"FAILED",
				message,
				failedPage,
				failureResponse
			)
		);
	}

	public AlioRecruitmentSyncProgressResponse get() {
		return get(GLOBAL_PROGRESS_KEY);
	}

	public AlioRecruitmentSyncProgressResponse get(String sessionId) {
		return progressByKey.getOrDefault(
			sessionId,
			new AlioRecruitmentSyncProgressResponse(
				false,
				0,
				0,
				0,
				0,
				0,
				"IDLE",
				"데이터 갱신 대기 중입니다.",
				0,
				null
			)
		);
	}
}
