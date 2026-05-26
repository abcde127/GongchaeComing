package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitmentSyncState;
import com.gongchae.gongchae_coming.alio.dto.AlioFilterOptionResponse;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentCategoryCountRow;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentMonthlyCountRow;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentStatisticsResponse;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentStatisticsRow;
import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentSyncStateRepository;
import com.gongchae.gongchae_coming.alio.repository.PublicInstitutionRepository;
import com.gongchae.gongchae_coming.notification.service.NewRecruitmentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlioRecruitmentService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final int SYNC_PAGE_SIZE = 1000;
	private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

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
	private final PublicInstitutionRepository publicInstitutionRepository;
	private final AlioRecruitmentSyncProgressStore syncProgressStore;
	private final NewRecruitmentNotificationService newRecruitmentNotificationService;
	private final AlioRecruitmentSeedExporter alioRecruitmentSeedExporter;
	private final AtomicBoolean syncInProgress = new AtomicBoolean(false);
	private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();

	@Transactional
	public JsonNode getRecruitments(AlioRecruitmentListRequest request) {
		return buildResponseFromCachedRecruitments(request);
	}

	@Transactional(readOnly = true)
	public AlioRecruitmentStatisticsResponse getRecruitmentStatistics() {
		List<AlioRecruitmentStatisticsRow> recruitments = alioRecruitmentRepository.findStatisticsRows();
		Map<String, Long> statusCounts = countRecruitmentsByStatus(recruitments);

		return new AlioRecruitmentStatisticsResponse(
			recruitments.size(),
			resolveLastFetchedAt(),
			List.of(
				new AlioRecruitmentStatisticsResponse.StatusCount("scheduled", "예정", statusCounts.get("scheduled")),
				new AlioRecruitmentStatisticsResponse.StatusCount("active", "진행", statusCounts.get("active")),
				new AlioRecruitmentStatisticsResponse.StatusCount("closed", "마감", statusCounts.get("closed"))
			),
			countRecruitmentsByMonthlyStart(recruitments),
			countRecruitmentsByRegion(recruitments)
		);
	}

	@Transactional(readOnly = true)
	public AlioRecruitmentStatisticsResponse.Summary getRecruitmentStatisticsSummary() {
		String today = todayBasicDate();
		return new AlioRecruitmentStatisticsResponse.Summary(
			alioRecruitmentRepository.count(),
			alioRecruitmentRepository.countScheduledRecruitments(today),
			alioRecruitmentRepository.countActiveRecruitments(today),
			resolveLastFetchedAt()
		);
	}

	@Transactional(readOnly = true)
	public List<AlioRecruitmentStatisticsResponse.MonthlyCount> getRecruitmentMonthlyStartCounts(String regionCode) {
		String normalizedRegionCode = normalizeRegionCode(regionCode);
		List<AlioRecruitmentMonthlyCountRow> rows = StringUtils.hasText(normalizedRegionCode)
			? alioRecruitmentRepository.findMonthlyStartCountRowsByRegionCode(normalizedRegionCode)
			: alioRecruitmentRepository.findMonthlyStartCountRows();
		return rows.stream()
			.map(row -> new AlioRecruitmentStatisticsResponse.MonthlyCount(row.getYearMonth(), row.getCount()))
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AlioRecruitmentStatisticsResponse.YearlyCount> getRecruitmentYearlyStartCounts(String regionCode) {
		return countRecruitmentsByYearlyStart(statisticsRows(regionCode));
	}

	@Transactional(readOnly = true)
	public List<AlioRecruitmentStatisticsResponse.CategoryCount> getRecruitmentNcsCounts(String regionCode) {
		return countRecruitmentsByNcs(statisticsRows(regionCode));
	}

	@Transactional(readOnly = true)
	public List<AlioRecruitmentStatisticsResponse.CategoryCount> getRecruitmentCompanyCounts(String regionCode) {
		String normalizedRegionCode = normalizeRegionCode(regionCode);
		List<AlioRecruitmentCategoryCountRow> rows = StringUtils.hasText(normalizedRegionCode)
			? alioRecruitmentRepository.findCompanyCountRowsByRegionCode(normalizedRegionCode)
			: alioRecruitmentRepository.findCompanyCountRows();
		return categoryCounts(rows);
	}

	@Transactional(readOnly = true)
	public List<AlioRecruitmentStatisticsResponse.CategoryCount> getRecruitmentCategoryCounts(String regionCode) {
		return countRecruitmentsByRecruitmentCategory(statisticsRows(regionCode));
	}

	@Transactional(readOnly = true)
	public List<AlioRecruitmentStatisticsResponse.CategoryCount> getRecruitmentHireTypeCounts(String regionCode) {
		return countRecruitmentsByHireType(statisticsRows(regionCode));
	}

	@Transactional(readOnly = true)
	public List<AlioRecruitmentStatisticsResponse.RegionCount> getRecruitmentRegionCounts() {
		return countRecruitmentsByRegion(alioRecruitmentRepository.findStatisticsRows());
	}

	@Transactional
	public List<AlioRecruitment> importRecruitments(List<JsonNode> items, LocalDateTime fetchedAt) {
		return upsertRecruitments(items, fetchedAt);
	}

	public boolean startBackgroundSynchronization(AlioRecruitmentListRequest request) {
		if (!syncInProgress.compareAndSet(false, true)) {
			return false;
		}
		syncProgressStore.start();

		syncExecutor.submit(() -> {
			try {
				synchronizeRecruitments(request);
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

	private void synchronizeRecruitments(AlioRecruitmentListRequest request) {
		LocalDateTime now = LocalDateTime.now();
		AlioRecruitmentListRequest syncRequest = request.withoutSearchAndFilters();
		int pageNo = 1;
		int newRecruitmentCount = 0;
		int syncedRecruitmentCount = 0;
		Integer totalCount = null;
		int totalPages = 0;
		List<AlioRecruitment> newRecruitments = new ArrayList<>();
		Long latestStoredRecruitmentSequence = alioRecruitmentRepository.findMaxRecrutPblntSn().orElse(null);
		long storedRecruitmentCount = alioRecruitmentRepository.count();
		boolean stopSynchronization = false;

		while (!stopSynchronization && (totalCount == null || pageNo <= totalPages)) {
			AlioRecruitmentListRequest pageRequest = syncRequest.withPage(pageNo, SYNC_PAGE_SIZE);
			JsonNode response = fetchRecruitmentsOrFail(pageRequest, pageNo, totalPages, syncedRecruitmentCount, totalCount);

			ArrayNode items = findRecruitmentItems(response);
			if (items == null || items.isEmpty()) {
				break;
			}

			totalCount = extractTotalCount(response);
			if (totalCount != null) {
				int expectedNewRecruitmentCount = Math.max(0, totalCount - saturatedInt(storedRecruitmentCount));
				totalPages = Math.max(1, (int) Math.ceil((double) expectedNewRecruitmentCount / SYNC_PAGE_SIZE));
			}
			List<JsonNode> pageItems = new ArrayList<>();
			items.forEach(pageItems::add);
			List<JsonNode> newPageItems = new ArrayList<>();
			for (JsonNode item : pageItems) {
				if (shouldStopSynchronization(item, latestStoredRecruitmentSequence)) {
					stopSynchronization = true;
					break;
				}
				newPageItems.add(item);
			}
			newRecruitments.addAll(upsertRecruitments(newPageItems, now));
			newRecruitmentCount += newPageItems.size();
			if (totalCount == null) {
				totalPages = Math.max(totalPages, pageNo + (items.size() == SYNC_PAGE_SIZE ? 1 : 0));
				syncedRecruitmentCount = newRecruitmentCount;
			} else {
				syncedRecruitmentCount = Math.min(totalCount, saturatedInt(storedRecruitmentCount) + newRecruitmentCount);
			}
			syncProgressStore.update(
				Math.min(pageNo, totalPages),
				totalPages,
				syncedRecruitmentCount,
				totalCount == null ? syncedRecruitmentCount : totalCount
			);
			if (totalCount == null && items.size() < SYNC_PAGE_SIZE) {
				break;
			}
			if (totalCount != null && pageNo >= totalPages) {
				break;
			}
			pageNo++;
		}

		int completedPages = totalPages == 0 ? 0 : Math.min(pageNo, totalPages);
		syncStateRepository.save(AlioRecruitmentSyncState.global(LocalDateTime.now()));
		syncProgressStore.complete(
			completedPages,
			totalPages,
			totalCount == null ? newRecruitmentCount : Math.min(totalCount, saturatedInt(storedRecruitmentCount) + newRecruitmentCount),
			totalCount == null ? newRecruitmentCount : totalCount
		);
		exportSeedRecruitments();
		newRecruitmentNotificationService.sendNewRecruitmentNotifications(newRecruitments);
	}

	private void exportSeedRecruitments() {
		try {
			alioRecruitmentSeedExporter.exportSeedRecruitments();
		} catch (Exception exception) {
			log.warn("Failed to export ALIO recruitment seed data after synchronization.", exception);
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

	private boolean shouldStopSynchronization(JsonNode item, Long latestStoredRecruitmentSequence) {
		Long recruitmentSequence = extractRecruitmentSequence(item);
		return latestStoredRecruitmentSequence != null
			&& recruitmentSequence != null
			&& recruitmentSequence <= latestStoredRecruitmentSequence;
	}

	private int saturatedInt(long value) {
		return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
	}

	@PreDestroy
	void shutdownSyncExecutor() {
		syncExecutor.shutdownNow();
	}

	private List<AlioRecruitment> upsertRecruitments(List<JsonNode> items, LocalDateTime fetchedAt) {
		Set<Long> recruitmentSequences = items.stream()
			.map(AlioRecruitment::resolveRecruitmentSequence)
			.filter(sequence -> sequence != null)
			.collect(Collectors.toSet());
		if (recruitmentSequences.isEmpty()) {
			return List.of();
		}
		Map<Long, AlioRecruitment> existingRecruitments = alioRecruitmentRepository
			.findByRecrutPblntSnIn(recruitmentSequences)
			.stream()
			.collect(Collectors.toMap(AlioRecruitment::getRecrutPblntSn, recruitment -> recruitment));
		List<AlioRecruitment> newRecruitments = new ArrayList<>();

		List<AlioRecruitment> recruitments = items.stream()
			.filter(item -> AlioRecruitment.resolveRecruitmentSequence(item) != null)
			.map(item -> {
				Long recruitmentSequence = AlioRecruitment.resolveRecruitmentSequence(item);
				AlioRecruitment recruitment = existingRecruitments.get(recruitmentSequence);
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
		long overallTotalCount = alioRecruitmentRepository.count();
		Page<AlioRecruitment> recruitments = alioRecruitmentRepository.findAll(
			recruitmentSpecification(request),
			pageable(request)
		);
		root.put("overallTotalCount", overallTotalCount);
		body.put("overallTotalCount", overallTotalCount);
		addFilterOptions(root);

		recruitments.forEach(recruitment -> {
			ObjectNode item = OBJECT_MAPPER.createObjectNode();
			recruitment.writeTo(item);
			item.put("ongoingYn", isOngoingRecruitment(item) ? "Y" : "N");
			itemArray.add(item);
		});
		LocalDateTime lastFetchedAt = syncStateRepository.findById(AlioRecruitmentSyncState.GLOBAL_ID)
			.map(AlioRecruitmentSyncState::getLastSucceededAt)
			.or(() -> alioRecruitmentRepository.findLatestCreatedAt())
			.orElse(null);
		if (lastFetchedAt != null) {
			root.put("lastFetchedAt", lastFetchedAt.toString());
			body.put("lastFetchedAt", lastFetchedAt.toString());
		}
		updateTotalCount(root, saturatedInt(recruitments.getTotalElements()));
		return root;
	}

	private String resolveLastFetchedAt() {
		return syncStateRepository.findById(AlioRecruitmentSyncState.GLOBAL_ID)
			.map(AlioRecruitmentSyncState::getLastSucceededAt)
			.or(() -> alioRecruitmentRepository.findLatestCreatedAt())
			.map(LocalDateTime::toString)
			.orElse(null);
	}

	private Map<String, Long> countRecruitmentsByStatus(List<AlioRecruitmentStatisticsRow> recruitments) {
		Map<String, Long> statusCounts = new LinkedHashMap<>();
		statusCounts.put("scheduled", 0L);
		statusCounts.put("active", 0L);
		statusCounts.put("closed", 0L);
		recruitments.forEach(recruitment ->
			statusCounts.computeIfPresent(resolveRecruitmentStatus(recruitment), (status, count) -> count + 1)
		);
		return statusCounts;
	}

	private List<AlioRecruitmentStatisticsResponse.MonthlyCount> countRecruitmentsByMonthlyStart(
		List<AlioRecruitmentStatisticsRow> recruitments
	) {
		Map<String, Long> monthlyStartCounts = new HashMap<>();
		recruitments.forEach(recruitment -> {
			String yearMonth = resolveYearMonth(recruitment);
			if (yearMonth != null) {
				monthlyStartCounts.merge(yearMonth, 1L, Long::sum);
			}
		});
		return monthlyStartCounts.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new AlioRecruitmentStatisticsResponse.MonthlyCount(entry.getKey(), entry.getValue()))
			.toList();
	}

	private List<AlioRecruitmentStatisticsResponse.YearlyCount> countRecruitmentsByYearlyStart(
		List<AlioRecruitmentStatisticsRow> recruitments
	) {
		Map<String, Long> yearlyStartCounts = new HashMap<>();
		recruitments.forEach(recruitment -> {
			LocalDate startDate = parseDate(recruitment.getPbancBgngYmd());
			if (startDate != null) {
				yearlyStartCounts.merge(String.valueOf(startDate.getYear()), 1L, Long::sum);
			}
		});
		return yearlyStartCounts.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new AlioRecruitmentStatisticsResponse.YearlyCount(entry.getKey(), entry.getValue()))
			.toList();
	}

	private List<AlioRecruitmentStatisticsResponse.CategoryCount> countRecruitmentsByNcs(
		List<AlioRecruitmentStatisticsRow> recruitments
	) {
		Map<String, CategoryAccumulator> ncsCounts = new HashMap<>();
		recruitments.forEach(recruitment -> addCategoryCounts(
			ncsCounts,
			splitCsv(recruitment.getNcsCdLst()),
			splitCsv(recruitment.getNcsCdNmLst())
		));
		return categoryCounts(ncsCounts);
	}

	private List<AlioRecruitmentStatisticsResponse.CategoryCount> countRecruitmentsByCompany(
		List<AlioRecruitmentStatisticsRow> recruitments
	) {
		Map<String, CategoryAccumulator> companyCounts = new HashMap<>();
		recruitments.forEach(recruitment -> {
			String code = recruitment.getPblntInstCd();
			String label = recruitment.getInstNm();
			if (!StringUtils.hasText(code) && !StringUtils.hasText(label)) {
				return;
			}
			if (!StringUtils.hasText(code)) {
				code = label;
			}
			if (!StringUtils.hasText(label)) {
				label = code;
			}
			addCategoryCount(companyCounts, code, label);
		});
		return categoryCounts(companyCounts);
	}

	private List<AlioRecruitmentStatisticsResponse.CategoryCount> countRecruitmentsByRecruitmentCategory(
		List<AlioRecruitmentStatisticsRow> recruitments
	) {
		Map<String, CategoryAccumulator> recruitmentCategoryCounts = new HashMap<>();
		recruitments.forEach(recruitment -> addCategoryCounts(
			recruitmentCategoryCounts,
			splitCsv(recruitment.getRecrutSe()),
			splitCsv(recruitment.getRecrutSeNm())
		));
		return categoryCounts(recruitmentCategoryCounts);
	}

	private List<AlioRecruitmentStatisticsResponse.CategoryCount> countRecruitmentsByHireType(
		List<AlioRecruitmentStatisticsRow> recruitments
	) {
		Map<String, CategoryAccumulator> hireTypeCounts = new HashMap<>();
		recruitments.forEach(recruitment -> addFixedOptionCounts(
			hireTypeCounts,
			splitCsv(recruitment.getHireTypeLst()),
			splitCsv(recruitment.getHireTypeNmLst()),
			HIRE_TYPE_FILTER_OPTIONS
		));
		return categoryCounts(hireTypeCounts);
	}

	private List<AlioRecruitmentStatisticsResponse.RegionCount> countRecruitmentsByRegion(
		List<AlioRecruitmentStatisticsRow> recruitments
	) {
		Map<String, RegionAccumulator> regionCounts = new HashMap<>();
		recruitments.forEach(recruitment -> addRegionCounts(regionCounts, recruitment));
		return regionCounts.values()
			.stream()
			.sorted(Comparator
				.comparingLong(RegionAccumulator::count)
				.reversed()
				.thenComparing(RegionAccumulator::label))
			.map(region -> new AlioRecruitmentStatisticsResponse.RegionCount(region.code(), region.label(), region.count()))
			.toList();
	}

	private String resolveRecruitmentStatus(AlioRecruitmentStatisticsRow recruitment) {
		LocalDate today = LocalDate.now();
		LocalDate startDate = parseDate(recruitment.getPbancBgngYmd());
		LocalDate endDate = parseDate(recruitment.getPbancEndYmd());
		if (startDate != null && startDate.isAfter(today)) {
			return "scheduled";
		}
		if (endDate != null && endDate.isBefore(today)) {
			return "closed";
		}
		return "active";
	}

	private String resolveYearMonth(AlioRecruitmentStatisticsRow recruitment) {
		LocalDate startDate = parseDate(recruitment.getPbancBgngYmd());
		return startDate == null ? null : startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
	}

	private void addRegionCounts(Map<String, RegionAccumulator> regionCounts, AlioRecruitmentStatisticsRow recruitment) {
		List<String> regionCodes = splitCsv(recruitment.getWorkRgnLst());
		List<String> regionLabels = splitCsv(recruitment.getWorkRgnNmLst());
		int regionSize = Math.max(regionCodes.size(), regionLabels.size());
		for (int index = 0; index < regionSize; index++) {
			String code = valueAt(regionCodes, index);
			String label = valueAt(regionLabels, index);
			if (!StringUtils.hasText(code) && !StringUtils.hasText(label)) {
				continue;
			}
			if (!StringUtils.hasText(label)) {
				label = regionLabelByCode(code);
			}
			if (!StringUtils.hasText(code)) {
				code = label;
			}
			String key = StringUtils.hasText(code) ? code : label;
			String resolvedCode = code;
			String resolvedLabel = label;
			regionCounts.compute(
				key,
				(ignored, accumulator) -> accumulator == null
					? new RegionAccumulator(resolvedCode, resolvedLabel, 1)
					: accumulator.increment()
			);
		}
	}

	private List<AlioRecruitmentStatisticsRow> statisticsRows(String regionCode) {
		String normalizedRegionCode = normalizeRegionCode(regionCode);
		if (!StringUtils.hasText(normalizedRegionCode)) {
			return alioRecruitmentRepository.findStatisticsRows();
		}
		return alioRecruitmentRepository.findStatisticsRowsByRegionCode(normalizedRegionCode);
	}

	private String normalizeRegionCode(String regionCode) {
		return StringUtils.hasText(regionCode) ? regionCode.trim() : null;
	}

	private void addCategoryCounts(
		Map<String, CategoryAccumulator> categoryCounts,
		List<String> codes,
		List<String> labels
	) {
		int categorySize = Math.max(codes.size(), labels.size());
		for (int index = 0; index < categorySize; index++) {
			String code = valueAt(codes, index);
			String label = valueAt(labels, index);
			if (!StringUtils.hasText(code) && !StringUtils.hasText(label)) {
				continue;
			}
			if (!StringUtils.hasText(code)) {
				code = label;
			}
			if (!StringUtils.hasText(label)) {
				label = code;
			}
			addCategoryCount(categoryCounts, code, label);
		}
	}

	private void addCategoryCount(Map<String, CategoryAccumulator> categoryCounts, String code, String label) {
		addCategoryCount(categoryCounts, code, label, 1);
	}

	private void addCategoryCount(
		Map<String, CategoryAccumulator> categoryCounts,
		String code,
		String label,
		long count
	) {
		categoryCounts.compute(
			code,
			(ignored, accumulator) -> accumulator == null
				? new CategoryAccumulator(code, label, count)
				: accumulator.increment(count)
		);
	}

	private void addFixedOptionCounts(
		Map<String, CategoryAccumulator> categoryCounts,
		List<String> codes,
		List<String> labels,
		List<AlioFilterOptionResponse> options
	) {
		Map<String, AlioFilterOptionResponse> optionsByCode = options.stream()
			.collect(Collectors.toMap(AlioFilterOptionResponse::code, option -> option));
		Map<String, AlioFilterOptionResponse> optionsByLabel = options.stream()
			.collect(Collectors.toMap(AlioFilterOptionResponse::name, option -> option));
		Set<String> matchedCodes = new HashSet<>();

		codes.stream()
			.map(optionsByCode::get)
			.filter(Objects::nonNull)
			.map(AlioFilterOptionResponse::code)
			.forEach(matchedCodes::add);
		labels.stream()
			.map(optionsByLabel::get)
			.filter(Objects::nonNull)
			.map(AlioFilterOptionResponse::code)
			.forEach(matchedCodes::add);

		matchedCodes.forEach(code -> {
			AlioFilterOptionResponse option = optionsByCode.get(code);
			addCategoryCount(categoryCounts, option.code(), option.name());
		});
	}

	private List<AlioRecruitmentStatisticsResponse.CategoryCount> categoryCounts(
		Map<String, CategoryAccumulator> categoryCounts
	) {
		return categoryCounts.values()
			.stream()
			.sorted(Comparator
				.comparingLong(CategoryAccumulator::count)
				.reversed()
				.thenComparing(CategoryAccumulator::label))
			.map(category -> new AlioRecruitmentStatisticsResponse.CategoryCount(
				category.code(),
				category.label(),
				category.count()
			))
			.toList();
	}

	private List<AlioRecruitmentStatisticsResponse.CategoryCount> categoryCounts(
		List<AlioRecruitmentCategoryCountRow> rows
	) {
		Map<String, CategoryAccumulator> categoryCounts = new HashMap<>();
		rows.forEach(row -> {
			String code = row.getCode();
			String label = row.getLabel();
			if (!StringUtils.hasText(code) && !StringUtils.hasText(label)) {
				return;
			}
			if (!StringUtils.hasText(code)) {
				code = label;
			}
			if (!StringUtils.hasText(label)) {
				label = code;
			}
			addCategoryCount(categoryCounts, code, label, row.getCount());
		});
		return categoryCounts(categoryCounts);
	}

	private List<String> splitCsv(String value) {
		if (!StringUtils.hasText(value)) {
			return List.of();
		}
		return List.of(value.split(","))
			.stream()
			.map(String::trim)
			.filter(this::hasMeaningfulText)
			.toList();
	}

	private boolean hasMeaningfulText(String value) {
		return StringUtils.hasText(value) && !"null".equalsIgnoreCase(value);
	}

	private String valueAt(List<String> values, int index) {
		return index < values.size() ? values.get(index) : null;
	}

	private String regionLabelByCode(String code) {
		return WORK_REGION_FILTER_OPTIONS.stream()
			.filter(region -> region.code().equals(code))
			.map(AlioFilterOptionResponse::name)
			.findFirst()
			.orElse(code);
	}

	private record RegionAccumulator(
		String code,
		String label,
		long count
	) {

		private RegionAccumulator increment() {
			return new RegionAccumulator(code, label, count + 1);
		}
	}

	private record CategoryAccumulator(
		String code,
		String label,
		long count
	) {

		private CategoryAccumulator increment() {
			return increment(1);
		}

		private CategoryAccumulator increment(long amount) {
			return new CategoryAccumulator(code, label, count + amount);
		}
	}

	private Pageable pageable(AlioRecruitmentListRequest request) {
		return PageRequest.of(request.resolvedPageNo() - 1, request.resolvedNumOfRows());
	}

	private void addFilterOptions(ObjectNode root) {
		ArrayNode companies = root.putObject("filterOptions").putArray("companies");
		publicInstitutionRepository.findAllByOrderByInstNmAsc()
			.stream()
			.map(publicInstitution -> publicInstitution.getInstNm())
			.filter(StringUtils::hasText)
			.distinct()
			.forEach(companies::add);
	}

	private Specification<AlioRecruitment> recruitmentSpecification(AlioRecruitmentListRequest request) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			String searchKeyword = request.resolvedRecruitmentTitleKeyword();
			if (StringUtils.hasText(searchKeyword)) {
				String normalizedKeyword = "%" + normalizeKeyword(searchKeyword) + "%";
				predicates.add(criteriaBuilder.or(
					criteriaBuilder.like(normalizedText(criteriaBuilder, root.get("recrutPbancTtl")), normalizedKeyword),
					criteriaBuilder.like(normalizedText(criteriaBuilder, root.get("instNm")), normalizedKeyword)
				));
			}

			addContainsAnyPredicate(
				predicates,
				criteriaBuilder,
				request.hireTypeLst(),
				root.get("hireTypeLst"),
				root.get("hireTypeNmLst")
			);
			addContainsAnyPredicate(
				predicates,
				criteriaBuilder,
				request.ncsCdLst(),
				root.get("ncsCdLst"),
				root.get("ncsCdNmLst")
			);
			addContainsAnyPredicate(
				predicates,
				criteriaBuilder,
				request.workRgnLst(),
				root.get("workRgnLst"),
				root.get("workRgnNmLst")
			);
			addContainsAnyPredicate(
				predicates,
				criteriaBuilder,
				request.recrutSe(),
				root.get("recrutSe"),
				root.get("recrutSeNm")
			);
			addContainsAnyPredicate(
				predicates,
				criteriaBuilder,
				request.acbgCondLst(),
				root.get("acbgCondLst"),
				root.get("acbgCondNmLst")
			);
			addRecruitmentStatusPredicate(
				predicates,
				criteriaBuilder,
				request.recruitmentStatus(),
				root.get("pbancBgngYmd"),
				root.get("pbancEndYmd")
			);
			addContainsAnyPredicate(
				predicates,
				criteriaBuilder,
				request.pblntInstCd(),
				root.get("pblntInstCd"),
				root.get("instNm")
			);

			if (StringUtils.hasText(request.replmprYn())) {
				predicates.add(criteriaBuilder.equal(root.get("replmprYn"), request.replmprYn()));
			}
			if ("Y".equals(request.ongoingYn())) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(
					normalizedDate(criteriaBuilder, root.get("pbancEndYmd")),
					todayBasicDate()
				));
			} else if ("N".equals(request.ongoingYn())) {
				predicates.add(criteriaBuilder.lessThan(normalizedDate(criteriaBuilder, root.get("pbancEndYmd")), todayBasicDate()));
			}
			if (StringUtils.hasText(request.pbancBgngYmd())) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(
					normalizedDate(criteriaBuilder, root.get("pbancBgngYmd")),
					toBasicDate(request.pbancBgngYmd())
				));
			}
			if (StringUtils.hasText(request.pbancEndYmd())) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(
					normalizedDate(criteriaBuilder, root.get("pbancEndYmd")),
					toBasicDate(request.pbancEndYmd())
				));
			}
			if ("DEADLINE_DATE".equals(request.resolvedSortBy())) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(
					normalizedDate(criteriaBuilder, root.get("pbancEndYmd")),
					todayBasicDate()
				));
			}

			if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
				applyOrder(request, query, criteriaBuilder, root);
			}

			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}

	@SafeVarargs
	private void addContainsAnyPredicate(
		List<Predicate> predicates,
		CriteriaBuilder criteriaBuilder,
		String csvValues,
		Expression<String>... fields
	) {
		if (!StringUtils.hasText(csvValues)) {
			return;
		}

		List<String> values = List.of(csvValues.split(","))
			.stream()
			.map(String::trim)
			.filter(StringUtils::hasText)
			.toList();
		if (values.isEmpty()) {
			return;
		}

		List<Predicate> valuePredicates = new ArrayList<>();
		for (String value : values) {
			String pattern = "%" + value + "%";
			for (Expression<String> field : fields) {
				valuePredicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(field, ""), pattern));
			}
		}
		predicates.add(criteriaBuilder.or(valuePredicates.toArray(Predicate[]::new)));
	}

	private void addRecruitmentStatusPredicate(
		List<Predicate> predicates,
		CriteriaBuilder criteriaBuilder,
		String csvValues,
		Expression<String> startDateField,
		Expression<String> endDateField
	) {
		if (!StringUtils.hasText(csvValues)) {
			return;
		}

		String today = todayBasicDate();
		List<Predicate> statusPredicates = List.of(csvValues.split(","))
			.stream()
			.map(String::trim)
			.filter(StringUtils::hasText)
			.map(status -> switch (status) {
				case "scheduled" -> criteriaBuilder.greaterThan(normalizedDate(criteriaBuilder, startDateField), today);
				case "active" -> criteriaBuilder.and(
					criteriaBuilder.lessThanOrEqualTo(normalizedDate(criteriaBuilder, startDateField), today),
					criteriaBuilder.greaterThanOrEqualTo(normalizedDate(criteriaBuilder, endDateField), today)
				);
				case "closed" -> criteriaBuilder.lessThan(normalizedDate(criteriaBuilder, endDateField), today);
				default -> null;
			})
			.filter(predicate -> predicate != null)
			.toList();
		if (!statusPredicates.isEmpty()) {
			predicates.add(criteriaBuilder.or(statusPredicates.toArray(Predicate[]::new)));
		}
	}

	private Expression<String> normalizedText(
		CriteriaBuilder criteriaBuilder,
		Expression<String> field
	) {
		return criteriaBuilder.lower(criteriaBuilder.function(
			"replace",
			String.class,
			criteriaBuilder.coalesce(field, ""),
			criteriaBuilder.literal(" "),
			criteriaBuilder.literal("")
		));
	}

	private Expression<String> normalizedDate(
		CriteriaBuilder criteriaBuilder,
		Expression<String> field
	) {
		return criteriaBuilder.function(
			"replace",
			String.class,
			criteriaBuilder.coalesce(field, ""),
			criteriaBuilder.literal("-"),
			criteriaBuilder.literal("")
		);
	}

	private void applyOrder(
		AlioRecruitmentListRequest request,
		CriteriaQuery<?> query,
		CriteriaBuilder criteriaBuilder,
		Root<AlioRecruitment> root
	) {
		boolean ascending = "ASC".equals(request.resolvedSortDirection());
		if ("DEADLINE_DATE".equals(request.resolvedSortBy())) {
			query.orderBy(
				ascending
					? criteriaBuilder.asc(normalizedDate(criteriaBuilder, root.get("pbancEndYmd")))
					: criteriaBuilder.desc(normalizedDate(criteriaBuilder, root.get("pbancEndYmd"))),
				criteriaBuilder.desc(root.get("recrutPblntSn")),
				criteriaBuilder.asc(root.get("recrutPbancTtl"))
			);
			return;
		}

		if (ascending && "RECRUITMENT_SEQUENCE".equals(request.resolvedSortBy())) {
			query.orderBy(criteriaBuilder.asc(root.get("recrutPblntSn")), criteriaBuilder.asc(root.get("recrutPbancTtl")));
			return;
		}

		query.orderBy(criteriaBuilder.desc(root.get("recrutPblntSn")), criteriaBuilder.asc(root.get("recrutPbancTtl")));
	}

	private String todayBasicDate() {
		return LocalDate.now().format(BASIC_DATE_FORMATTER);
	}

	private String toBasicDate(String value) {
		return LocalDate.parse(value).format(BASIC_DATE_FORMATTER);
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

	private boolean isOngoingRecruitment(JsonNode item) {
		LocalDate today = LocalDate.now();
		LocalDate endDate = parseDate(item, "pbancEndYmd");
		return endDate != null && !today.isAfter(endDate);
	}

	private String normalizeKeyword(String value) {
		return StringUtils.hasText(value)
			? value.replaceAll("\\s+", "").toLowerCase()
			: "";
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
			LocalDate parsedDate = parseDate(value);
			if (parsedDate != null) {
				return parsedDate;
			}
		}

		return null;
	}

	private LocalDate parseDate(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		try {
			if (value.trim().matches("^\\d{8}$")) {
				return LocalDate.parse(value.trim(), java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
			}
			return LocalDate.parse(value.trim());
		} catch (DateTimeParseException ignored) {
			return null;
		}
	}
}
