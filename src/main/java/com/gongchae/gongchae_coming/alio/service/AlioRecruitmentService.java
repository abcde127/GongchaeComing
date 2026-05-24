package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitmentSyncState;
import com.gongchae.gongchae_coming.alio.dto.AlioFilterOptionResponse;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentSyncProgressResponse;
import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentSyncStateRepository;
import com.gongchae.gongchae_coming.notification.service.NewRecruitmentNotificationService;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlioRecruitmentService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final int SYNC_PAGE_SIZE = 1000;

	private static final List<AlioFilterOptionResponse> NCS_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("R600001", "사업관리"),
		new AlioFilterOptionResponse("R600002", "경영.회계.사무"),
		new AlioFilterOptionResponse("R600003", "금융.보험"),
		new AlioFilterOptionResponse("R600004", "교육.자연.사회과학"),
		new AlioFilterOptionResponse("R600005", "법률.경찰.소방.교도.국방"),
		new AlioFilterOptionResponse("R600006", "보건.의료"),
		new AlioFilterOptionResponse("R600007", "사회복지.종교"),
		new AlioFilterOptionResponse("R600008", "문화.예술.디자인.방송"),
		new AlioFilterOptionResponse("R600009", "운전.운송"),
		new AlioFilterOptionResponse("R600010", "영업판매"),
		new AlioFilterOptionResponse("R600011", "경비.청소"),
		new AlioFilterOptionResponse("R600012", "이용.숙박.여행.오락.스포츠"),
		new AlioFilterOptionResponse("R600013", "음식서비스"),
		new AlioFilterOptionResponse("R600014", "건설"),
		new AlioFilterOptionResponse("R600015", "기계"),
		new AlioFilterOptionResponse("R600016", "재료"),
		new AlioFilterOptionResponse("R600017", "화학"),
		new AlioFilterOptionResponse("R600018", "섬유.의복"),
		new AlioFilterOptionResponse("R600019", "전기.전자"),
		new AlioFilterOptionResponse("R600020", "정보통신"),
		new AlioFilterOptionResponse("R600021", "식품가공"),
		new AlioFilterOptionResponse("R600022", "인쇄.목재.가구.공예"),
		new AlioFilterOptionResponse("R600023", "환경.에너지.안전"),
		new AlioFilterOptionResponse("R600024", "농림어업"),
		new AlioFilterOptionResponse("R600025", "연구")
	);

	private static final List<AlioFilterOptionResponse> WORK_REGION_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("R3010", "서울특별시"),
		new AlioFilterOptionResponse("R3011", "인천광역시"),
		new AlioFilterOptionResponse("R3012", "대전광역시"),
		new AlioFilterOptionResponse("R3013", "대구광역시"),
		new AlioFilterOptionResponse("R3014", "부산광역시"),
		new AlioFilterOptionResponse("R3015", "광주광역시"),
		new AlioFilterOptionResponse("R3016", "울산광역시"),
		new AlioFilterOptionResponse("R3017", "경기도"),
		new AlioFilterOptionResponse("R3018", "강원도"),
		new AlioFilterOptionResponse("R3019", "충청남도"),
		new AlioFilterOptionResponse("R3020", "충청북도"),
		new AlioFilterOptionResponse("R3021", "경상북도"),
		new AlioFilterOptionResponse("R3022", "경상남도"),
		new AlioFilterOptionResponse("R3023", "전라남도"),
		new AlioFilterOptionResponse("R3024", "전라북도"),
		new AlioFilterOptionResponse("R3025", "제주특별자치도"),
		new AlioFilterOptionResponse("R3026", "세종특별자치시"),
		new AlioFilterOptionResponse("R3030", "해외")
	);

	private static final List<AlioFilterOptionResponse> HIRE_TYPE_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("R1010", "정규직"),
		new AlioFilterOptionResponse("R1020", "계약직"),
		new AlioFilterOptionResponse("R1030", "무기계약직"),
		new AlioFilterOptionResponse("R1040", "비정규직"),
		new AlioFilterOptionResponse("R1050", "청년인턴"),
		new AlioFilterOptionResponse("R1060", "청년인턴(체험형)"),
		new AlioFilterOptionResponse("R1070", "청년인턴(채용형)")
	);

	private static final List<AlioFilterOptionResponse> INSTITUTION_TYPE_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("A2001", "공기업(시장형)"),
		new AlioFilterOptionResponse("A2002", "공기업(준시장형)"),
		new AlioFilterOptionResponse("A2003", "준정부기관(기금관리형)"),
		new AlioFilterOptionResponse("A2004", "준정부기관(위탁집행형)"),
		new AlioFilterOptionResponse("A2005", "기타공공기관")
	);

	private static final List<AlioFilterOptionResponse> SORT_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("RECRUITMENT_SEQUENCE", "최근등록순"),
		new AlioFilterOptionResponse("DEADLINE_DATE", "마감임박순")
	);

	private static final List<AlioFilterOptionResponse> SORT_DIRECTION_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("DESC", "내림차순"),
		new AlioFilterOptionResponse("ASC", "오름차순")
	);

	private static final List<AlioFilterOptionResponse> PAGE_SIZE_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("10", "10개씩 보기"),
		new AlioFilterOptionResponse("20", "20개씩 보기"),
		new AlioFilterOptionResponse("30", "30개씩 보기"),
		new AlioFilterOptionResponse("50", "50개씩 보기")
	);

	private final AlioRecruitmentClient alioRecruitmentClient;
	private final AlioRecruitmentRepository alioRecruitmentRepository;
	private final AlioRecruitmentSyncStateRepository syncStateRepository;
	private final AlioRecruitmentSyncProgressStore syncProgressStore;
	private final NewRecruitmentNotificationService newRecruitmentNotificationService;
	private final AtomicBoolean syncInProgress = new AtomicBoolean(false);
	private final AtomicBoolean syncCancelRequested = new AtomicBoolean(false);
	private final AtomicBoolean syncPauseRequested = new AtomicBoolean(false);
	private final Object syncPauseMonitor = new Object();
	private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();

	@Transactional
	public JsonNode getRecruitments(AlioRecruitmentListRequest request) {
		return getRecruitments(request, false);
	}

	@Transactional
	public JsonNode getRecruitments(AlioRecruitmentListRequest request, boolean refresh) {
		return getRecruitments(request, refresh, false, null);
	}

	@Transactional
	public JsonNode getRecruitments(AlioRecruitmentListRequest request, boolean refresh, String progressKey) {
		return getRecruitments(request, refresh, false, progressKey);
	}

	@Transactional
	public JsonNode getRecruitments(
		AlioRecruitmentListRequest request,
		boolean refresh,
		boolean resume,
		String progressKey
	) {
		if (refresh) {
			startBackgroundSynchronization(request, resume);
		}

		ObjectNode response = buildResponseFromCachedRecruitments(request);
		filterRecruitmentItems(response, request);
		sortRecruitmentItems(response, request.resolvedSortBy(), request.resolvedSortDirection());
		pageRecruitmentItems(response, request.resolvedPageNo(), request.resolvedNumOfRows());
		return response;
	}

	public boolean startBackgroundSynchronization(AlioRecruitmentListRequest request) {
		return startBackgroundSynchronization(request, false);
	}

	public boolean startBackgroundSynchronization(AlioRecruitmentListRequest request, boolean resume) {
		if (!syncInProgress.compareAndSet(false, true)) {
			return false;
		}
		syncCancelRequested.set(false);
		syncPauseRequested.set(false);
		AlioRecruitmentSyncProgressResponse previousProgress = syncProgressStore.get();
		syncProgressStore.start();

		syncExecutor.submit(() -> {
			try {
				synchronizeRecruitments(request, resume, previousProgress);
			} catch (Exception exception) {
				if (!"FAILED".equals(syncProgressStore.get().status())) {
					syncProgressStore.fail(
						0,
						0,
						0,
						0,
						"데이터 갱신 중 오류가 발생했습니다.",
						0,
						failureResponseFromException(exception)
					);
				}
			} finally {
				syncInProgress.set(false);
			}
		});
		return true;
	}

	public boolean cancelBackgroundSynchronization() {
		if (!syncInProgress.get()) {
			return false;
		}
		syncCancelRequested.set(true);
		syncPauseRequested.set(false);
		syncProgressStore.canceling();
		synchronized (syncPauseMonitor) {
			syncPauseMonitor.notifyAll();
		}
		return true;
	}

	public boolean pauseBackgroundSynchronization() {
		if (!syncInProgress.get()) {
			return false;
		}
		syncPauseRequested.set(true);
		syncProgressStore.paused();
		return true;
	}

	public boolean resumePausedSynchronization() {
		if (!syncInProgress.get() || !syncPauseRequested.get()) {
			return false;
		}
		syncPauseRequested.set(false);
		syncProgressStore.resumed();
		synchronized (syncPauseMonitor) {
			syncPauseMonitor.notifyAll();
		}
		return true;
	}

	private void synchronizeRecruitments(
		AlioRecruitmentListRequest request,
		boolean resume,
		AlioRecruitmentSyncProgressResponse previousProgress
	) {
		LocalDateTime now = LocalDateTime.now();
		AlioRecruitmentListRequest syncRequest = request.withoutSearchAndFilters();
		int pageNo = resume && previousProgress.failedPage() > 0 ? previousProgress.failedPage() : 1;
		int fetchedCount = resume ? previousProgress.fetchedCount() : 0;
		Integer totalCount = null;
		if (resume && previousProgress.totalCount() > 0) {
			totalCount = previousProgress.totalCount();
		}
		int totalPages = resume ? previousProgress.totalPages() : 0;
		List<AlioRecruitment> newRecruitments = new ArrayList<>();

		while (totalCount == null || fetchedCount < totalCount) {
			waitWhileSynchronizationPaused();
			if (syncCancelRequested.get()) {
				syncProgressStore.canceled(pageNo - 1, totalPages, fetchedCount, totalCount == null ? 0 : totalCount);
				return;
			}
			AlioRecruitmentListRequest pageRequest = syncRequest.withPage(pageNo, SYNC_PAGE_SIZE);
			JsonNode response = fetchRecruitmentsOrFail(pageRequest, pageNo, totalPages, fetchedCount, totalCount);

			ArrayNode items = findRecruitmentItems(response);
			if (items == null || items.isEmpty()) {
				break;
			}

			totalCount = extractTotalCount(response);
			List<JsonNode> pageItems = new ArrayList<>();
			items.forEach(pageItems::add);
			newRecruitments.addAll(upsertRecruitments(pageItems, now));
			fetchedCount += pageItems.size();
			totalPages = totalCount == null
				? Math.max(totalPages, pageNo + (items.size() == SYNC_PAGE_SIZE ? 1 : 0))
				: Math.max(1, (int) Math.ceil((double) totalCount / SYNC_PAGE_SIZE));
			syncProgressStore.update(pageNo, totalPages, fetchedCount, totalCount == null ? 0 : totalCount);
			if (syncCancelRequested.get()) {
				syncProgressStore.canceled(pageNo, totalPages, fetchedCount, totalCount == null ? 0 : totalCount);
				return;
			}
			waitWhileSynchronizationPaused();
			if (syncCancelRequested.get()) {
				syncProgressStore.canceled(pageNo, totalPages, fetchedCount, totalCount == null ? 0 : totalCount);
				return;
			}
			if (totalCount == null && items.size() < SYNC_PAGE_SIZE) {
				break;
			}
			pageNo++;
		}

		int completedPages = totalPages == 0 ? 0 : Math.min(pageNo, totalPages);
		if (totalCount != null && fetchedCount < totalCount) {
			ObjectNode failureResponse = OBJECT_MAPPER.createObjectNode();
			failureResponse.put("message", "API totalCount보다 적은 데이터만 수집된 상태로 갱신이 종료되었습니다.");
			failureResponse.put("fetchedCount", fetchedCount);
			failureResponse.put("totalCount", totalCount);
			syncProgressStore.fail(
				completedPages,
				totalPages,
				fetchedCount,
				totalCount,
				"전체 데이터 갱신을 완료하지 못했습니다.",
				pageNo,
				failureResponse
			);
			return;
		}
		syncStateRepository.save(AlioRecruitmentSyncState.global(LocalDateTime.now()));
		syncProgressStore.complete(
			completedPages,
			totalPages,
			fetchedCount,
			totalCount == null ? fetchedCount : totalCount
		);
		newRecruitmentNotificationService.sendNewRecruitmentNotifications(newRecruitments);
	}

	private void waitWhileSynchronizationPaused() {
		while (syncPauseRequested.get() && !syncCancelRequested.get()) {
			syncProgressStore.paused();
			synchronized (syncPauseMonitor) {
				try {
					syncPauseMonitor.wait(1000);
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					syncCancelRequested.set(true);
				}
			}
		}
	}

	private JsonNode fetchRecruitmentsOrFail(
		AlioRecruitmentListRequest request,
		int pageNo,
		int totalPages,
		int fetchedCount,
		Integer totalCount
	) {
		try {
			JsonNode response = alioRecruitmentClient.fetchRecruitments(request);
			if (!isAlioErrorResponse(response)) {
				return response;
			}
			failSynchronization(pageNo, totalPages, fetchedCount, totalCount, response);
			throw new IllegalStateException("ALIO API returned error response.");
		} catch (RuntimeException exception) {
			if (!"FAILED".equals(syncProgressStore.get().status())) {
				JsonNode failureResponse = failureResponseFromException(exception);
				failSynchronization(pageNo, totalPages, fetchedCount, totalCount, failureResponse);
			}
			throw exception;
		}
	}

	private void failSynchronization(
		int pageNo,
		int totalPages,
		int fetchedCount,
		Integer totalCount,
		JsonNode failureResponse
	) {
		syncProgressStore.fail(
			pageNo,
			totalPages,
			fetchedCount,
			totalCount == null ? 0 : totalCount,
			"%d페이지 호출에 실패했습니다.".formatted(pageNo),
			pageNo,
			failureResponse
		);
	}

	private JsonNode failureResponseFromException(Exception exception) {
		if (exception instanceof AlioApiException alioApiException
			&& StringUtils.hasText(alioApiException.getAlioResponseBody())) {
			try {
				return OBJECT_MAPPER.readTree(alioApiException.getAlioResponseBody());
			} catch (Exception ignored) {
				ObjectNode response = OBJECT_MAPPER.createObjectNode();
				response.put("responseBody", alioApiException.getAlioResponseBody());
				return response;
			}
		}

		ObjectNode response = OBJECT_MAPPER.createObjectNode();
		response.put("message", exception.getMessage());
		return response;
	}

	@PreDestroy
	void shutdownSyncExecutor() {
		syncExecutor.shutdownNow();
	}

	private List<AlioRecruitment> upsertRecruitments(List<JsonNode> items, LocalDateTime fetchedAt) {
		Set<String> sourceIds = items.stream()
			.map(AlioRecruitment::resolveSourceRecruitmentId)
			.collect(Collectors.toSet());
		if (sourceIds.isEmpty()) {
			return List.of();
		}
		Map<String, AlioRecruitment> existingRecruitments = alioRecruitmentRepository
			.findBySourceRecruitmentIdIn(sourceIds)
			.stream()
			.collect(Collectors.toMap(AlioRecruitment::getSourceRecruitmentId, recruitment -> recruitment));
		List<AlioRecruitment> newRecruitments = new ArrayList<>();

		List<AlioRecruitment> recruitments = items.stream()
			.map(item -> {
				String sourceId = AlioRecruitment.resolveSourceRecruitmentId(item);
				AlioRecruitment recruitment = existingRecruitments.get(sourceId);
				if (recruitment == null) {
					AlioRecruitment newRecruitment = AlioRecruitment.from(item, fetchedAt);
					newRecruitments.add(newRecruitment);
					return newRecruitment;
				}
				recruitment.updateFrom(item, fetchedAt);
				return recruitment;
			})
			.toList();

		alioRecruitmentRepository.saveAll(recruitments);
		return newRecruitments;
	}

	private ObjectNode buildResponseFromCachedRecruitments(AlioRecruitmentListRequest request) {
		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		root.put("resultCode", "00");
		root.put("resultMsg", "NORMAL SERVICE");
		ObjectNode response = root.putObject("response");
		ObjectNode body = response.putObject("body");
		body.put("pageNo", request.resolvedPageNo());
		body.put("numOfRows", request.resolvedNumOfRows());
		ObjectNode items = body.putObject("items");
		ArrayNode itemArray = items.putArray("item");
		List<AlioRecruitment> recruitments = alioRecruitmentRepository.findAll();
		root.put("overallTotalCount", recruitments.size());
		body.put("overallTotalCount", recruitments.size());
		addFilterOptions(root, recruitments);

		recruitments.forEach(recruitment -> {
			ObjectNode item = OBJECT_MAPPER.createObjectNode();
			recruitment.writeTo(item);
			itemArray.add(item);
		});
		LocalDateTime lastFetchedAt = syncStateRepository.findById(AlioRecruitmentSyncState.GLOBAL_ID)
			.map(AlioRecruitmentSyncState::getLastSucceededAt)
			.orElse(null);
		if (lastFetchedAt != null) {
			root.put("lastFetchedAt", lastFetchedAt.toString());
			body.put("lastFetchedAt", lastFetchedAt.toString());
		}
		updateTotalCount(root, itemArray.size());
		return root;
	}

	private void addFilterOptions(ObjectNode root, List<AlioRecruitment> recruitments) {
		ArrayNode companies = root.putObject("filterOptions").putArray("companies");
		recruitments.stream()
			.map(recruitment -> {
				ObjectNode item = OBJECT_MAPPER.createObjectNode();
				recruitment.writeTo(item);
				return firstNonBlank(
					item.path("pblntInstNm").asText(""),
					item.path("instNm").asText("")
				);
			})
			.filter(StringUtils::hasText)
			.collect(Collectors.toCollection(LinkedHashSet::new))
			.stream()
			.sorted()
			.forEach(companies::add);
	}

	private void attachDebugInfoWhenAlioReturnsError(AlioRecruitmentListRequest request, JsonNode response) {
		if (!(response instanceof ObjectNode responseObject) || !isAlioErrorResponse(response)) {
			return;
		}

		ObjectNode debugNode = responseObject.putObject("_debug");
		debugNode.put("alioRequestMethod", alioRecruitmentClient.buildRequestMethodForDebug());
		debugNode.put("alioRequestUri", alioRecruitmentClient.buildRequestUriForDebug(request));
		debugNode.put("searchKeyword", safeText(request.searchKeyword()));
		debugNode.put("recrutPbancTtl", safeText(request.resolvedRecruitmentTitleKeyword()));
		debugNode.put("resultType", safeText(request.resultType()));
	}

	private boolean isAlioErrorResponse(JsonNode response) {
		String resultCode = response.path("resultCode").asText(null);
		return StringUtils.hasText(resultCode) && !"00".equals(resultCode) && !"200".equals(resultCode);
	}

	private String safeText(String value) {
		return value == null ? "" : value;
	}

	private void filterRecruitmentItems(JsonNode response, AlioRecruitmentListRequest request) {
		ArrayNode items = findRecruitmentItems(response);
		if (items == null) {
			return;
		}

		List<Predicate<JsonNode>> predicates = buildPredicates(request);
		List<JsonNode> filteredItems = new ArrayList<>();

		items.forEach(item -> {
			if (predicates.stream().allMatch(predicate -> predicate.test(item))) {
				filteredItems.add(item);
			}
		});

		items.removeAll();
		items.addAll(filteredItems);
		updateTotalCount(response, filteredItems.size());
	}

	private List<Predicate<JsonNode>> buildPredicates(AlioRecruitmentListRequest request) {
		List<Predicate<JsonNode>> predicates = new ArrayList<>();
		String searchKeyword = request.resolvedRecruitmentTitleKeyword();

		if (StringUtils.hasText(searchKeyword)) {
			String normalizedKeyword = normalizeKeyword(searchKeyword);
			predicates.add(item -> {
				String title = normalizeKeyword(item.path("recrutPbancTtl").asText(""));
				String institution = normalizeKeyword(firstNonBlank(
					item.path("pblntInstNm").asText(""),
					item.path("instNm").asText("")
				));
				return title.contains(normalizedKeyword) || institution.contains(normalizedKeyword);
			});
		}

		addContainsAnyPredicate(predicates, request.hireTypeLst(), "hireTypeLst", "hireTypeNmLst");
		addContainsAnyPredicate(predicates, request.instType(), "instType", "instTypeNm");
		addContainsAnyPredicate(predicates, request.ncsCdLst(), "ncsCdLst", "ncsCdNmLst");
		addContainsAnyPredicate(predicates, request.workRgnLst(), "workRgnLst", "workRgnNmLst");
		addContainsAnyPredicate(predicates, request.recrutSe(), "recrutSe", "recrutSeNm");
		addContainsAnyPredicate(predicates, request.acbgCondLst(), "acbgCondLst", "acbgCondNmLst");
		addRecruitmentStatusPredicate(predicates, request.recruitmentStatus());

		if (StringUtils.hasText(request.instClsf())) {
			predicates.add(item -> item.path("instClsf").asText("").contains(request.instClsf()));
		}
		addContainsAnyPredicate(predicates, request.pblntInstCd(), "pblntInstCd", "pblntInstNm", "instNm");
		if (StringUtils.hasText(request.replmprYn())) {
			predicates.add(item -> request.replmprYn().equals(item.path("replmprYn").asText("")));
		}
		if ("Y".equals(request.ongoingYn())) {
			predicates.add(item -> "Y".equals(item.path("ongoingYn").asText(null)) || isOngoingRecruitment(item));
		} else if ("N".equals(request.ongoingYn())) {
			predicates.add(item -> "N".equals(item.path("ongoingYn").asText(null)) || !isOngoingRecruitment(item));
		}
		if (StringUtils.hasText(request.pbancBgngYmd())) {
			LocalDate startDate = LocalDate.parse(request.pbancBgngYmd());
			predicates.add(item -> {
				LocalDate itemStartDate = parseDate(item, "pbancBgngYmd", "pbancRgtrYmd");
				return itemStartDate != null && !itemStartDate.isBefore(startDate);
			});
		}
		if (StringUtils.hasText(request.pbancEndYmd())) {
			LocalDate endDate = LocalDate.parse(request.pbancEndYmd());
			predicates.add(item -> {
				LocalDate itemEndDate = parseDate(item, "pbancEndYmd", "aplyEndYmd");
				return itemEndDate != null && !itemEndDate.isAfter(endDate);
			});
		}

		return predicates;
	}

	private void addRecruitmentStatusPredicate(List<Predicate<JsonNode>> predicates, String csvValues) {
		if (!StringUtils.hasText(csvValues)) {
			return;
		}

		Set<String> statuses = List.of(csvValues.split(","))
			.stream()
			.map(String::trim)
			.filter(StringUtils::hasText)
			.collect(Collectors.toSet());

		predicates.add(item -> statuses.contains(resolveRecruitmentStatus(item)));
	}

	private String resolveRecruitmentStatus(JsonNode item) {
		LocalDate today = LocalDate.now();
		LocalDate startDate = parseDate(item, "pbancBgngYmd", "pbancRgtrYmd");
		LocalDate endDate = parseDate(item, "pbancEndYmd", "aplyEndYmd", "endDate");
		if (startDate == null || endDate == null) {
			return null;
		}
		if (today.isBefore(startDate)) {
			return "scheduled";
		}
		if (today.isAfter(endDate)) {
			return "closed";
		}
		return "active";
	}

	private void addContainsAnyPredicate(
		List<Predicate<JsonNode>> predicates,
		String csvValues,
		String... fieldNames
	) {
		if (!StringUtils.hasText(csvValues)) {
			return;
		}

		List<String> values = List.of(csvValues.split(","))
			.stream()
			.map(String::trim)
			.filter(StringUtils::hasText)
			.toList();

		predicates.add(item -> values.stream().anyMatch(value -> {
			for (String fieldName : fieldNames) {
				if (item.path(fieldName).asText("").contains(value)) {
					return true;
				}
			}
			return false;
		}));
	}

	private boolean isOngoingRecruitment(JsonNode item) {
		LocalDate today = LocalDate.now();
		LocalDate startDate = parseDate(item, "pbancBgngYmd", "pbancRgtrYmd");
		LocalDate endDate = parseDate(item, "pbancEndYmd", "aplyEndYmd");
		return startDate != null && endDate != null
			&& !today.isBefore(startDate)
			&& !today.isAfter(endDate);
	}

	private String normalizeKeyword(String value) {
		return StringUtils.hasText(value)
			? value.replaceAll("\\s+", "").toLowerCase()
			: "";
	}

	private String firstNonBlank(String first, String second) {
		if (StringUtils.hasText(first)) {
			return first;
		}
		return second;
	}

	private void updateTotalCount(JsonNode response, int totalCount) {
		if (response instanceof ObjectNode rootObject) {
			rootObject.put("totalCount", totalCount);
		}

		JsonNode bodyNode = response.path("response").path("body");
		if (bodyNode instanceof ObjectNode bodyObject) {
			bodyObject.put("totalCount", totalCount);
		}
	}

	private Integer extractTotalCount(JsonNode response) {
		if (response.hasNonNull("totalCount")) {
			return response.path("totalCount").asInt();
		}

		JsonNode bodyNode = response.path("response").path("body");
		if (bodyNode.hasNonNull("totalCount")) {
			return bodyNode.path("totalCount").asInt();
		}

		return null;
	}

	private void sortRecruitmentItems(JsonNode response, String sortBy, String sortDirection) {
		ArrayNode items = findRecruitmentItems(response);
		if (items == null) {
			return;
		}

		List<JsonNode> sortedItems = new ArrayList<>();
		items.forEach(sortedItems::add);

		if ("DEADLINE_DATE".equals(sortBy)) {
			sortedItems = new ArrayList<>(sortedItems.stream()
				.filter(item -> !isClosedRecruitment(item))
				.toList());
			items.removeAll();
			items.addAll(sortedItems);
			updateTotalCount(response, sortedItems.size());
		}

		if (sortedItems.size() < 2) {
			return;
		}

		if ("RECRUITMENT_SEQUENCE".equals(sortBy)) {
			Comparator<Long> sequenceComparator = "ASC".equals(sortDirection)
				? Comparator.naturalOrder()
				: Comparator.reverseOrder();
			sortedItems.sort(Comparator
				.comparing((JsonNode item) -> extractRecruitmentSequence(item), Comparator.nullsLast(sequenceComparator))
				.thenComparing(item -> extractSortDate(item, "REGISTRATION_DATE"), Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(item -> item.path("recrutPbancTtl").asText("")));
			items.removeAll();
			items.addAll(sortedItems);
			return;
		}

		Comparator<LocalDate> dateComparator = "ASC".equals(sortDirection)
			? Comparator.naturalOrder()
			: Comparator.reverseOrder();

		Comparator<JsonNode> comparator = Comparator
			.comparing((JsonNode item) -> extractSortDate(item, sortBy), Comparator.nullsLast(dateComparator))
			.thenComparing(item -> extractSortDate(item, "REGISTRATION_DATE"), Comparator.nullsLast(dateComparator))
			.thenComparing(item -> item.path("recrutPbancTtl").asText(""));

		sortedItems.sort(comparator);
		items.removeAll();
		items.addAll(sortedItems);
	}

	private ArrayNode findRecruitmentItems(JsonNode response) {
		JsonNode resultNode = response.path("result");
		if (resultNode.isArray()) {
			return (ArrayNode) resultNode;
		}

		JsonNode responseNode = response.path("response");
		JsonNode bodyNode = responseNode.path("body");
		JsonNode itemsNode = bodyNode.path("items");

		if (itemsNode.isArray()) {
			return (ArrayNode) itemsNode;
		}

		JsonNode itemNode = itemsNode.path("item");
		if (itemNode.isArray()) {
			return (ArrayNode) itemNode;
		}

		JsonNode bodyItemNode = bodyNode.path("item");
		if (bodyItemNode.isArray()) {
			return (ArrayNode) bodyItemNode;
		}

		return null;
	}

	private void pageRecruitmentItems(JsonNode response, int pageNo, int numOfRows) {
		ArrayNode items = findRecruitmentItems(response);
		if (items == null) {
			return;
		}

		List<JsonNode> allItems = new ArrayList<>();
		items.forEach(allItems::add);
		int fromIndex = Math.min((pageNo - 1) * numOfRows, allItems.size());
		int toIndex = Math.min(fromIndex + numOfRows, allItems.size());

		items.removeAll();
		items.addAll(allItems.subList(fromIndex, toIndex));
	}

	private LocalDate extractSortDate(JsonNode item, String sortBy) {
		if ("DEADLINE_DATE".equals(sortBy)) {
			LocalDate endDate = parseDate(item, "pbancEndYmd");
			if (endDate != null) {
				return endDate;
			}

			return parseDate(item, "aplyEndYmd", "endDate");
		}

		LocalDate registrationDate = parseDate(item, "pbancBgngYmd");
		if (registrationDate != null) {
			return registrationDate;
		}

		return parseDate(item, "pbancRgtrYmd", "regDt", "frstRegDt", "registrationDate");
	}

	private boolean isClosedRecruitment(JsonNode item) {
		LocalDate endDate = parseDate(item, "pbancEndYmd", "aplyEndYmd", "endDate");
		return endDate != null && LocalDate.now().isAfter(endDate);
	}

	private Long extractRecruitmentSequence(JsonNode item) {
		JsonNode sequenceNode = item.path("recrutPblntSn");
		if (sequenceNode.isIntegralNumber()) {
			return sequenceNode.asLong();
		}
		String sequenceText = sequenceNode.asText(null);
		if (!StringUtils.hasText(sequenceText)) {
			return null;
		}
		try {
			return Long.parseLong(sequenceText.trim());
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	private LocalDate parseDate(JsonNode item, String... fieldNames) {
		for (String fieldName : fieldNames) {
			String value = item.path(fieldName).asText(null);
			if (!StringUtils.hasText(value)) {
				continue;
			}

			try {
				if (value.trim().matches("^\\d{8}$")) {
					return LocalDate.parse(value.trim(), java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
				}
				return LocalDate.parse(value.trim());
			} catch (DateTimeParseException ignored) {
				// Ignore non-ISO date values and continue with fallback fields.
			}
		}

		return null;
	}
}
