package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitmentSyncState;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentStatisticsRow;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentSyncStateRepository;
import com.gongchae.gongchae_coming.alio.repository.PublicInstitutionRepository;
import com.gongchae.gongchae_coming.notification.service.NewRecruitmentNotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlioRecruitmentServiceTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void getRecruitmentsSortsItemsByRegistrationDateByDefault() {
		ObjectNode response = createResponse(
			recruitment("second", "2026-04-15", "2026-04-20"),
			recruitment("first", "2026-04-01", "2026-04-10")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request(null));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("first");
	}

	@Test
	void getRecruitmentsSortsItemsByRecruitmentSequenceDescendingForRegistrationDate() {
		ObjectNode response = createResponse(
			recruitment("second", "2026-04-15", "2026-04-20"),
			recruitment("first", "2026-04-01", "2026-04-10")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request("REGISTRATION_DATE", "ASC"));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("first");
	}

	@Test
	void getRecruitmentsSortsItemsByDeadlineDateWhenRequested() {
		ObjectNode response = createResponse(
			recruitment("second", date(LocalDate.now().minusDays(1)), date(LocalDate.now().plusDays(2))),
			recruitment("first", date(LocalDate.now().minusDays(1)), date(LocalDate.now().plusDays(5)))
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request("DEADLINE_DATE"));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("first");
	}

	@Test
	void getRecruitmentsExcludesClosedItemsWhenSortingByDeadlineDate() {
		ObjectNode response = createResponse(
			recruitment("active", date(LocalDate.now().minusDays(1)), date(LocalDate.now().plusDays(1)))
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request("DEADLINE_DATE"));

		assertThat(result.at("/response/body/items/item")).hasSize(1);
		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("active");
		assertThat(result.at("/totalCount").asInt()).isEqualTo(1);
	}

	@Test
	void startBackgroundSynchronizationRefreshesCachedData() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("totalCount", 1);
		apiResponse.putArray("result").add(recruitment("fresh", "2026-04-01", "2026-04-10"));
		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		stubRecruitmentPage(recruitmentRepository, createResponse(
			recruitment("cached", "2026-04-01", "2026-04-10")
		));
		when(recruitmentRepository.findMaxRecrutPblntSn()).thenReturn(Optional.empty());
		when(recruitmentRepository.findByRecrutPblntSnIn(any())).thenReturn(List.of());
		when(syncStateRepository.findById(any())).thenReturn(Optional.empty());

		service.startBackgroundSynchronization(request("REGISTRATION_DATE", null));
		var result = service.getRecruitments(request("REGISTRATION_DATE", null));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("cached");
		org.mockito.Mockito.verify(client, org.mockito.Mockito.timeout(1000))
			.fetchRecruitments(any(AlioRecruitmentListRequest.class));
	}

	@Test
	void getRecruitmentsFiltersItemsBySearchKeywordAgainstTitleAndInstitution() {
		ObjectNode response = createResponse(
			recruitment("일반 행정 채용", "국민건강보험공단", "2026-04-03", "2026-04-12"),
			recruitment("국민건강보험공단 체험형 인턴", "국민건강보험공단", "2026-04-01", "2026-04-10")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request("REGISTRATION_DATE", "DESC", "건강보험공단"));

		assertThat(result.at("/response/body/items/item")).hasSize(2);
		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("일반 행정 채용");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("국민건강보험공단 체험형 인턴");
		assertThat(result.at("/totalCount").asInt()).isEqualTo(2);
		assertThat(result.at("/response/body/totalCount").asInt()).isEqualTo(2);
	}

	@Test
	void getRecruitmentsReturnsRequestedPageFromCachedItems() {
		ObjectNode response = createResponse(
			recruitment("second", "2026-04-02", "2026-04-11")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response, 3);

		var result = service.getRecruitments(new AlioRecruitmentListRequest(
			null,
			null,
			null,
			null,
			null,
			1,
			null,
			null,
			2,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			"REGISTRATION_DATE",
			"ASC",
			null
		));

		assertThat(result.at("/response/body/items/item")).hasSize(1);
		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/totalCount").asInt()).isEqualTo(3);
	}

	@Test
	void getRecruitmentsKeepsServiceListItemColumnsInResponse() {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		item.put("recrutPblntSn", 300658);
		item.put("pblntInstCd", "C0661");
		item.put("instNm", "식품안전정보원");
		item.put("ncsCdLst", "R600001,R600002");
		item.put("ncsCdNmLst", "사업관리,경영.회계.사무");
		item.put("hireTypeLst", "R1040");
		item.put("hireTypeNmLst", "비정규직");
		item.put("workRgnLst", "R3010");
		item.put("workRgnNmLst", "서울");
		item.put("recrutSe", "R2020");
		item.put("recrutSeNm", "경력");
		item.put("pbancBgngYmd", "20260515");
		item.put("pbancEndYmd", "20260601");
		item.put("recrutPbancTtl", "식품안전정보원 개방형 직위 공개 모집");
		item.put("srcUrl", "https://www.foodinfo.or.kr");
		item.put("replmprYn", "N");
		item.put("acbgCondLst", "R7050,R7070");
		item.put("acbgCondNmLst", "대졸(4년),박사");
		AlioRecruitmentService service = serviceWithCachedItems(createResponse(item));

		var result = service.getRecruitments(request("REGISTRATION_DATE", "DESC"));
		var resultItem = result.at("/response/body/items/item/0");

		assertThat(result.path("lastFetchedAt").asText()).isNotBlank();
		assertThat(resultItem.path("recrutPblntSn").asLong()).isEqualTo(300658);
		assertThat(resultItem.path("pbancBgngYmd").asText()).isEqualTo("20260515");
		assertThat(resultItem.path("ongoingYn").asText()).isEqualTo("Y");
		assertThat(resultItem.path("pbadmsStdInstCd").isMissingNode()).isTrue();
		assertThat(resultItem.path("aplyQlfcCn").isMissingNode()).isTrue();
	}

	@Test
	void getRecruitmentsUsesLatestRecruitmentCreatedAtWhenSyncStateIsMissing() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		LocalDateTime latestCreatedAt = LocalDateTime.of(2026, 5, 24, 22, 30);
		stubRecruitmentPage(recruitmentRepository, createResponse(
			recruitment("신규 공고", "20260515", "20260601")
		));
		when(syncStateRepository.findById(any())).thenReturn(Optional.empty());
		when(recruitmentRepository.findLatestCreatedAt()).thenReturn(Optional.of(latestCreatedAt));

		var result = service.getRecruitments(request("REGISTRATION_DATE", "DESC"));

		assertThat(result.path("lastFetchedAt").asText()).isEqualTo("2026-05-24T22:30");
		assertThat(result.at("/response/body/lastFetchedAt").asText()).isEqualTo("2026-05-24T22:30");
	}

	@Test
	void getRecruitmentStatisticsCountsStatusesMonthsAndRegions() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode scheduled = recruitment("예정 공고", date(LocalDate.now().plusDays(2)), date(LocalDate.now().plusDays(10)));
		scheduled.put("workRgnLst", "R3010,R3017");
		scheduled.put("workRgnNmLst", "서울,경기");
		ObjectNode active = recruitment("진행 공고", date(LocalDate.now().minusDays(2)), date(LocalDate.now().plusDays(2)));
		active.put("workRgnLst", "R3010");
		active.put("workRgnNmLst", "서울");
		ObjectNode closed = recruitment("마감 공고", date(LocalDate.now().minusDays(10)), date(LocalDate.now().minusDays(1)));
		closed.put("workRgnLst", "R3026");

		when(recruitmentRepository.findStatisticsRows()).thenReturn(toStatisticsRows(createResponse(scheduled, active, closed)));
		when(syncStateRepository.findById(any())).thenReturn(Optional.of(
			AlioRecruitmentSyncState.global(LocalDateTime.of(2026, 5, 24, 10, 30))
		));

		var result = service.getRecruitmentStatistics();

		assertThat(result.totalCount()).isEqualTo(3);
		assertThat(result.lastFetchedAt()).isEqualTo("2026-05-24T10:30");
		assertThat(result.statusCounts())
			.extracting("status", "count")
			.containsExactly(
				org.assertj.core.groups.Tuple.tuple("scheduled", 1L),
				org.assertj.core.groups.Tuple.tuple("active", 1L),
				org.assertj.core.groups.Tuple.tuple("closed", 1L)
			);
		assertThat(result.monthlyStartCounts())
			.extracting("yearMonth")
			.contains(date(LocalDate.now().plusDays(2)).substring(0, 7));
		assertThat(result.regionCounts())
			.extracting("label", "count")
			.contains(
				org.assertj.core.groups.Tuple.tuple("서울", 2L),
				org.assertj.core.groups.Tuple.tuple("경기", 1L),
			org.assertj.core.groups.Tuple.tuple("세종특별자치시", 1L)
			);
	}

	@Test
	void getRecruitmentStatisticsSummaryUsesDedicatedCountQueries() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		when(recruitmentRepository.count()).thenReturn(100L);
		when(recruitmentRepository.countScheduledRecruitments(any())).thenReturn(7L);
		when(recruitmentRepository.countActiveRecruitments(any())).thenReturn(23L);
		when(syncStateRepository.findById(any())).thenReturn(Optional.of(
			AlioRecruitmentSyncState.global(LocalDateTime.of(2026, 5, 24, 10, 30))
		));

		var result = service.getRecruitmentStatisticsSummary();

		assertThat(result.totalCount()).isEqualTo(100L);
		assertThat(result.scheduledCount()).isEqualTo(7L);
		assertThat(result.activeCount()).isEqualTo(23L);
		assertThat(result.referenceAt()).isEqualTo("2026-05-24T10:30");
		verify(recruitmentRepository).countScheduledRecruitments(any());
		verify(recruitmentRepository).countActiveRecruitments(any());
	}

	@Test
	void regionStatisticCountsFilterRecruitmentsByRegion() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode seoul = recruitment("서울 공고", "2026-05-01", "2026-05-20");
		seoul.put("workRgnLst", "R3010");
		seoul.put("workRgnNmLst", "서울");
		seoul.put("pblntInstCd", "C001");
		seoul.put("instNm", "서울기관");
		seoul.put("ncsCdLst", "R600001,R600002");
		seoul.put("ncsCdNmLst", "사업관리,경영.회계.사무");
		ObjectNode busan = recruitment("부산 공고", "2025-03-01", "2025-03-20");
		busan.put("workRgnLst", "R3014");
		busan.put("workRgnNmLst", "부산");
		busan.put("pblntInstCd", "C002");
		busan.put("instNm", "부산기관");
		busan.put("ncsCdLst", "R600003");
		busan.put("ncsCdNmLst", "금융.보험");

		when(recruitmentRepository.findStatisticsRows()).thenReturn(toStatisticsRows(createResponse(seoul, busan)));

		assertThat(service.getRecruitmentYearlyStartCounts("R3010"))
			.extracting("year", "count")
			.containsExactly(org.assertj.core.groups.Tuple.tuple("2026", 1L));
		assertThat(service.getRecruitmentNcsCounts("R3010"))
			.extracting("label", "count")
			.contains(
				org.assertj.core.groups.Tuple.tuple("사업관리", 1L),
				org.assertj.core.groups.Tuple.tuple("경영.회계.사무", 1L)
			);
		assertThat(service.getRecruitmentCompanyCounts("R3010"))
			.extracting("label", "count")
			.containsExactly(org.assertj.core.groups.Tuple.tuple("서울기관", 1L));
	}

	@Test
	void startBackgroundSynchronizationRefreshesFromRootResultArrayWithOneThousandRows() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("resultMsg", "성공했습니다.");
		apiResponse.put("totalCount", 1);
		ArrayNode resultArray = apiResponse.putArray("result");
		resultArray.add(recruitment("식품안전정보원 개방형 직위 공개 모집", "20260515", "20260601"));

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		when(recruitmentRepository.findMaxRecrutPblntSn()).thenReturn(Optional.empty());
		when(recruitmentRepository.findByRecrutPblntSnIn(any())).thenReturn(List.of());
		stubRecruitmentPage(recruitmentRepository, createResponse(
			recruitment("식품안전정보원 개방형 직위 공개 모집", "20260515", "20260601")
		));
		when(syncStateRepository.findById(any())).thenReturn(Optional.empty());

		service.startBackgroundSynchronization(request("REGISTRATION_DATE", "DESC"));
		var result = service.getRecruitments(request("REGISTRATION_DATE", "DESC"));

		verify(client, org.mockito.Mockito.timeout(1000)).fetchRecruitments(argThat(request ->
			request.resolvedPageNo() == 1 && request.resolvedNumOfRows() == 1000
		));
		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText())
			.isEqualTo("식품안전정보원 개방형 직위 공개 모집");
	}

	@Test
	void completedRefreshSendsNewRecruitmentNotificationsAndExportsSeed() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		NewRecruitmentNotificationService notificationService = mock(NewRecruitmentNotificationService.class);
		AlioRecruitmentSeedExporter seedExporter = mock(AlioRecruitmentSeedExporter.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			notificationService,
			seedExporter
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("totalCount", 2);
		apiResponse.putArray("result")
			.add(recruitment("신규 공고", "20260515", "20260601"))
			.add(recruitment("기존 공고", "20260515", "20260601"));
		AlioRecruitment existingRecruitment = AlioRecruitment.from(
			recruitment("기존 공고", "20260515", "20260601"),
			LocalDateTime.now()
		);

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		when(recruitmentRepository.findMaxRecrutPblntSn()).thenReturn(Optional.empty());
		when(recruitmentRepository.findByRecrutPblntSnIn(any())).thenReturn(List.of(existingRecruitment));
		when(recruitmentRepository.findAll()).thenReturn(List.of());
		when(syncStateRepository.findById(any())).thenReturn(Optional.empty());

		service.startBackgroundSynchronization(request("REGISTRATION_DATE", "DESC"));

		verify(notificationService, org.mockito.Mockito.timeout(1000))
			.sendNewRecruitmentNotifications(argThat(recruitments ->
				recruitments.size() == 1 && titleOf(recruitments.get(0)).equals("신규 공고")
			));
		verify(seedExporter, org.mockito.Mockito.timeout(1000)).exportSeedRecruitments();
	}

	@Test
	void refreshStopsWhenStoredRecruitmentSequenceIsReached() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentSyncProgressStore progressStore = new AlioRecruitmentSyncProgressStore();
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			progressStore,
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("totalCount", 3);
		apiResponse.putArray("result")
			.add(recruitment("신규 공고", 103L, "20260515", date(LocalDate.now().plusDays(10))))
			.add(recruitment("이미 저장된 공고", 102L, "20260515", date(LocalDate.now().plusDays(10))))
			.add(recruitment("오래된 공고", 101L, "20260515", date(LocalDate.now().plusDays(10))));

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		when(recruitmentRepository.findMaxRecrutPblntSn()).thenReturn(Optional.of(102L));
		when(recruitmentRepository.findByRecrutPblntSnIn(any())).thenReturn(List.of());

		service.startBackgroundSynchronization(request("RECRUITMENT_SEQUENCE", "DESC"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Iterable<AlioRecruitment>> captor = ArgumentCaptor.forClass(Iterable.class);
		verify(recruitmentRepository, org.mockito.Mockito.timeout(1000)).saveAll(captor.capture());
		assertThat(captor.getValue())
			.extracting(this::titleOf)
			.containsExactly("신규 공고");
		waitUntilStatus(progressStore, "COMPLETED");
		assertThat(progressStore.get().status()).isEqualTo("COMPLETED");
	}

	@Test
	void refreshDoesNotStopWhenDeadlineIsBeforeSynchronizationDate() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentSyncProgressStore progressStore = new AlioRecruitmentSyncProgressStore();
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			progressStore,
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("totalCount", 3);
		apiResponse.putArray("result")
			.add(recruitment("마감 전 공고", 103L, "20260515", date(LocalDate.now().plusDays(10))))
			.add(recruitment("마감 지난 공고", 102L, "20260515", date(LocalDate.now().minusDays(1))))
			.add(recruitment("중단 이후 공고", 101L, "20260515", date(LocalDate.now().plusDays(10))));

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		when(recruitmentRepository.findMaxRecrutPblntSn()).thenReturn(Optional.empty());
		when(recruitmentRepository.findByRecrutPblntSnIn(any())).thenReturn(List.of());

		service.startBackgroundSynchronization(request("RECRUITMENT_SEQUENCE", "DESC"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Iterable<AlioRecruitment>> captor = ArgumentCaptor.forClass(Iterable.class);
		verify(recruitmentRepository, org.mockito.Mockito.timeout(1000)).saveAll(captor.capture());
		assertThat(captor.getValue())
			.extracting(this::titleOf)
			.containsExactly("마감 전 공고", "마감 지난 공고", "중단 이후 공고");
		waitUntilStatus(progressStore, "COMPLETED");
		assertThat(progressStore.get().status()).isEqualTo("COMPLETED");
	}

	@Test
	void refreshProgressUsesStoredCountAndApiTotalCount() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentSyncProgressStore progressStore = mock(AlioRecruitmentSyncProgressStore.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			progressStore,
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("totalCount", 1003);
		apiResponse.putArray("result")
			.add(recruitment("신규 공고", 1003L, "20260515", date(LocalDate.now().plusDays(10))))
			.add(recruitment("이미 저장된 공고", 1002L, "20260515", date(LocalDate.now().plusDays(10))));

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		when(recruitmentRepository.findMaxRecrutPblntSn()).thenReturn(Optional.of(1002L));
		when(recruitmentRepository.count()).thenReturn(1002L);
		when(recruitmentRepository.findByRecrutPblntSnIn(any())).thenReturn(List.of());

		service.startBackgroundSynchronization(request("RECRUITMENT_SEQUENCE", "DESC"));

		verify(progressStore, org.mockito.Mockito.timeout(1000)).update(1, 1, 1003, 1003);
		verify(progressStore, org.mockito.Mockito.timeout(1000)).complete(1, 1, 1003, 1003);
	}

	@Test
	void failedRefreshStoresFailureResponseWithoutAutomaticRetry() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentSyncProgressStore progressStore = new AlioRecruitmentSyncProgressStore();
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			progressStore,
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
		ObjectNode errorResponse = OBJECT_MAPPER.createObjectNode();
		errorResponse.put("resultCode", "500");
		errorResponse.put("resultMsg", "TEMPORARY_ERROR");

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(errorResponse);
		when(recruitmentRepository.findMaxRecrutPblntSn()).thenReturn(Optional.empty());
		when(recruitmentRepository.findAll()).thenReturn(List.of());

		service.startBackgroundSynchronization(request("REGISTRATION_DATE", "DESC"));

		verify(client, org.mockito.Mockito.timeout(1000).times(1)).fetchRecruitments(any(AlioRecruitmentListRequest.class));
		waitUntilStatus(progressStore, "FAILED");
		assertThat(progressStore.get().status()).isEqualTo("FAILED");
		assertThat(progressStore.get().failedPage()).isEqualTo(1);
		assertThat(progressStore.get().failureResponse().path("resultMsg").asText()).isEqualTo("TEMPORARY_ERROR");
	}

	private void waitUntilStatus(AlioRecruitmentSyncProgressStore progressStore, String status) {
		long deadline = System.currentTimeMillis() + 1000;
		while (!status.equals(progressStore.get().status()) && System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private AlioRecruitmentService serviceWithCachedItems(ObjectNode response) {
		return serviceWithCachedItems(response, toRecruitments(response).size());
	}

	private AlioRecruitmentService serviceWithCachedItems(ObjectNode response, long totalCount) {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		List<AlioRecruitment> recruitments = toRecruitments(response);

		when(recruitmentRepository.count()).thenReturn(totalCount);
		when(recruitmentRepository.findAll(any(Specification.class), any(Pageable.class)))
			.thenReturn(new PageImpl<>(recruitments, Pageable.unpaged(), totalCount));
		when(syncStateRepository.findById(any())).thenReturn(Optional.of(
			AlioRecruitmentSyncState.global(LocalDateTime.of(2026, 5, 16, 10, 30))
		));
		return new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			mock(PublicInstitutionRepository.class),
			new AlioRecruitmentSyncProgressStore(),
			mock(NewRecruitmentNotificationService.class),
			mock(AlioRecruitmentSeedExporter.class)
		);
	}

	private void stubRecruitmentPage(AlioRecruitmentRepository recruitmentRepository, ObjectNode response) {
		List<AlioRecruitment> recruitments = toRecruitments(response);
		when(recruitmentRepository.count()).thenReturn((long) recruitments.size());
		when(recruitmentRepository.findAll(any(Specification.class), any(Pageable.class)))
			.thenReturn(new PageImpl<>(recruitments, Pageable.unpaged(), recruitments.size()));
	}

	private List<AlioRecruitment> toRecruitments(ObjectNode response) {
		ArrayNode items = (ArrayNode) response.at("/response/body/items/item");
		return Arrays.stream(OBJECT_MAPPER.convertValue(items, ObjectNode[].class))
			.map(item -> AlioRecruitment.from(item, LocalDateTime.now()))
			.toList();
	}

	private List<AlioRecruitmentStatisticsRow> toStatisticsRows(ObjectNode response) {
		return toRecruitments(response)
			.stream()
			.<AlioRecruitmentStatisticsRow>map(recruitment -> {
				ObjectNode item = OBJECT_MAPPER.createObjectNode();
				recruitment.writeTo(item);
				return new AlioRecruitmentStatisticsRow() {
					@Override
					public String getPbancBgngYmd() {
						return item.path("pbancBgngYmd").asText(null);
					}

					@Override
					public String getPbancEndYmd() {
						return item.path("pbancEndYmd").asText(null);
					}

					@Override
					public String getWorkRgnLst() {
						return item.path("workRgnLst").asText(null);
					}

					@Override
					public String getWorkRgnNmLst() {
						return item.path("workRgnNmLst").asText(null);
					}

					@Override
					public String getPblntInstCd() {
						return item.path("pblntInstCd").asText(null);
					}

					@Override
					public String getInstNm() {
						return item.path("instNm").asText(null);
					}

					@Override
					public String getNcsCdLst() {
						return item.path("ncsCdLst").asText(null);
					}

					@Override
					public String getNcsCdNmLst() {
						return item.path("ncsCdNmLst").asText(null);
					}
				};
			})
			.toList();
	}

	private AlioRecruitmentListRequest request(String sortBy) {
		return request(sortBy, null);
	}

	private AlioRecruitmentListRequest request(String sortBy, String sortDirection) {
		return request(sortBy, sortDirection, null);
	}

	private AlioRecruitmentListRequest request(String sortBy, String sortDirection, String searchKeyword) {
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
			searchKeyword,
			null,
			null,
			null,
			null,
			sortBy,
			sortDirection,
			null
		);
	}

	private ObjectNode createResponse(ObjectNode... recruitments) {
		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		ObjectNode response = root.putObject("response");
		ObjectNode body = response.putObject("body");
		ObjectNode items = body.putObject("items");
		ArrayNode itemArray = items.putArray("item");

		for (ObjectNode recruitment : recruitments) {
			itemArray.add(recruitment);
		}

		return root;
	}

	private ObjectNode recruitment(String title, String registrationDate, String deadlineDate) {
		return recruitment(title, (String) null, registrationDate, deadlineDate);
	}

	private String date(LocalDate date) {
		return date.toString();
	}

	private ObjectNode recruitment(String title, String institution, String registrationDate, String deadlineDate) {
		ObjectNode node = OBJECT_MAPPER.createObjectNode();
		node.put("recrutPblntSn", recruitmentSequence(title, registrationDate));
		node.put("recrutPbancTtl", title);
		if (institution != null) {
			node.put("instNm", institution);
		}
		node.put("pbancBgngYmd", registrationDate);
		node.put("pbancEndYmd", deadlineDate);
		return node;
	}

	private ObjectNode recruitment(String title, Long recruitmentSequence, String registrationDate, String deadlineDate) {
		ObjectNode node = recruitment(title, registrationDate, deadlineDate);
		node.put("recrutPblntSn", recruitmentSequence);
		return node;
	}

	private long recruitmentSequence(String title, String registrationDate) {
		String datePart = registrationDate == null ? "" : registrationDate.replaceAll("\\D", "");
		long baseSequence = datePart.isBlank() ? 0 : Long.parseLong(datePart);
		return baseSequence * 1000 + Math.abs(title.hashCode() % 1000);
	}

	private String titleOf(AlioRecruitment recruitment) {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		recruitment.writeTo(item);
		return item.path("recrutPbancTtl").asText();
	}
}
