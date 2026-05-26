package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentSyncProgressResponse;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class AlioRecruitmentSyncProgressStore {

	public static final String GLOBAL_PROGRESS_KEY = "GLOBAL";

	private final Map<String, AlioRecruitmentSyncProgressResponse> progressByKey = new ConcurrentHashMap<>();
	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	public SseEmitter subscribe() {
		SseEmitter emitter = new SseEmitter(0L);
		emitters.add(emitter);
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(() -> emitters.remove(emitter));
		emitter.onError(exception -> emitters.remove(emitter));
		send(emitter, get());
		return emitter;
	}

	public void start() {
		start(GLOBAL_PROGRESS_KEY);
	}

	public void startPreparing() {
		put(GLOBAL_PROGRESS_KEY, new AlioRecruitmentSyncProgressResponse(
			true,
			0,
			0,
			0,
			0,
			0,
			"RUNNING",
			"데이터 갱신 준비 중입니다.",
			0,
			null
		));
	}

	public void start(String sessionId) {
		put(sessionId, new AlioRecruitmentSyncProgressResponse(
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
		put(
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
		put(
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

	public void fail(
		int currentPage,
		int totalPages,
		int fetchedCount,
		int totalCount,
		String message,
		int failedPage,
		JsonNode failureResponse
	) {
		put(
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

	private void put(String sessionId, AlioRecruitmentSyncProgressResponse progress) {
		progressByKey.put(sessionId, progress);
		if (GLOBAL_PROGRESS_KEY.equals(sessionId)) {
			publish(progress);
		}
	}

	private void publish(AlioRecruitmentSyncProgressResponse progress) {
		emitters.forEach(emitter -> send(emitter, progress));
	}

	private void send(SseEmitter emitter, AlioRecruitmentSyncProgressResponse progress) {
		try {
			emitter.send(SseEmitter.event()
				.name("progress")
				.data(progress));
		} catch (IOException | IllegalStateException exception) {
			emitters.remove(emitter);
		}
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
