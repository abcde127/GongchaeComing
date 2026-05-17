package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitmentSyncState;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentSyncStateRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

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
			recruitment("first", "2026-04-01", "2026-04-10"),
			recruitment("second", "2026-04-15", "2026-04-20")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request(null));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("first");
	}

	@Test
	void getRecruitmentsSortsItemsByRegistrationDateAscendingWhenRequested() {
		ObjectNode response = createResponse(
			recruitment("first", "2026-04-01", "2026-04-10"),
			recruitment("second", "2026-04-15", "2026-04-20")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request("REGISTRATION_DATE", "ASC"));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("first");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("second");
	}

	@Test
	void getRecruitmentsSortsItemsByDeadlineDateWhenRequested() {
		ObjectNode response = createResponse(
			recruitment("first", "2026-04-20", "2026-04-21"),
			recruitment("second", "2026-04-10", "2026-04-25")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(request("DEADLINE_DATE"));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("first");
	}

	@Test
	void getRecruitmentsStartsBackgroundRefreshAndReturnsCachedData() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			new AlioRecruitmentSyncProgressStore()
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("totalCount", 1);
		apiResponse.putArray("result").add(recruitment("fresh", "2026-04-01", "2026-04-10"));
		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		when(recruitmentRepository.findAll()).thenReturn(toRecruitments(createResponse(
			recruitment("cached", "2026-04-01", "2026-04-10")
		)));
		when(recruitmentRepository.findBySourceRecruitmentIdIn(any())).thenReturn(List.of());
		when(syncStateRepository.findById(any())).thenReturn(Optional.empty());

		var result = service.getRecruitments(request("REGISTRATION_DATE", null), true);

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("cached");
		org.mockito.Mockito.verify(client, org.mockito.Mockito.timeout(1000))
			.fetchRecruitments(any(AlioRecruitmentListRequest.class));
	}

	@Test
	void getRecruitmentsFiltersItemsBySearchKeywordAgainstTitleAndInstitution() {
		ObjectNode response = createResponse(
			recruitment("국민건강보험공단 체험형 인턴", "국민건강보험공단", "2026-04-01", "2026-04-10"),
			recruitment("한국전력공사 신입 채용", "한국전력공사", "2026-04-02", "2026-04-11"),
			recruitment("일반 행정 채용", "국민건강보험공단", "2026-04-03", "2026-04-12")
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
			recruitment("first", "2026-04-01", "2026-04-10"),
			recruitment("second", "2026-04-02", "2026-04-11"),
			recruitment("third", "2026-04-03", "2026-04-12")
		);
		AlioRecruitmentService service = serviceWithCachedItems(response);

		var result = service.getRecruitments(new AlioRecruitmentListRequest(
			null,
			null,
			null,
			null,
			null,
			1,
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
	void getRecruitmentsKeepsAlioListItemColumnsInResponse() {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		item.put("recrutPblntSn", 300658);
		item.put("pblntInstCd", "C0661");
		item.put("pbadmsStdInstCd", "B552995");
		item.put("instNm", "식품안전정보원");
		item.put("ncsCdLst", "R600001,R600002");
		item.put("ncsCdNmLst", "사업관리,경영.회계.사무");
		item.put("hireTypeLst", "R1040");
		item.put("hireTypeNmLst", "비정규직");
		item.put("workRgnLst", "R3010");
		item.put("workRgnNmLst", "서울");
		item.put("recrutSe", "R2020");
		item.put("recrutSeNm", "경력");
		item.put("prefCondCn", "공통우대사항");
		item.put("recrutNope", 2);
		item.put("pbancBgngYmd", "20260515");
		item.put("pbancEndYmd", "20260601");
		item.put("recrutPbancTtl", "식품안전정보원 개방형 직위 공개 모집");
		item.put("srcUrl", "https://www.foodinfo.or.kr");
		item.put("replmprYn", "N");
		item.put("aplyQlfcCn", "지원자격");
		item.put("disqlfcRsn", "결격사유");
		item.put("scrnprcdrMthdExpln", "서류전형");
		item.put("prefCn", "우대내용");
		item.put("acbgCondLst", "R7050,R7070");
		item.put("acbgCondNmLst", "대졸(4년),박사");
		item.putNull("nonatchRsn");
		item.put("ongoingYn", "Y");
		item.put("decimalDay", 16);
		item.putArray("files");
		item.putArray("steps");
		AlioRecruitmentService service = serviceWithCachedItems(createResponse(item));

		var result = service.getRecruitments(request("REGISTRATION_DATE", "DESC"));
		var resultItem = result.at("/response/body/items/item/0");

		assertThat(result.path("lastFetchedAt").asText()).isNotBlank();
		assertThat(resultItem.path("recrutPblntSn").asLong()).isEqualTo(300658);
		assertThat(resultItem.path("pbadmsStdInstCd").asText()).isEqualTo("B552995");
		assertThat(resultItem.path("prefCondCn").asText()).isEqualTo("공통우대사항");
		assertThat(resultItem.path("recrutNope").asInt()).isEqualTo(2);
		assertThat(resultItem.path("pbancBgngYmd").asText()).isEqualTo("20260515");
		assertThat(resultItem.path("ongoingYn").asText()).isEqualTo("Y");
		assertThat(resultItem.path("decimalDay").asInt()).isEqualTo(16);
		assertThat(resultItem.path("files").isArray()).isTrue();
		assertThat(resultItem.path("steps").isArray()).isTrue();
	}

	@Test
	void getRecruitmentsRefreshesFromRootResultArrayWithOneThousandRows() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);
		AlioRecruitmentService service = new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			new AlioRecruitmentSyncProgressStore()
		);
		ObjectNode apiResponse = OBJECT_MAPPER.createObjectNode();
		apiResponse.put("resultCode", 200);
		apiResponse.put("resultMsg", "성공했습니다.");
		apiResponse.put("totalCount", 1);
		ArrayNode resultArray = apiResponse.putArray("result");
		resultArray.add(recruitment("식품안전정보원 개방형 직위 공개 모집", "20260515", "20260601"));

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(apiResponse);
		when(recruitmentRepository.findBySourceRecruitmentIdIn(any())).thenReturn(List.of());
		when(recruitmentRepository.findAll()).thenReturn(toRecruitments(createResponse(
			recruitment("식품안전정보원 개방형 직위 공개 모집", "20260515", "20260601")
		)));
		when(syncStateRepository.findById(any())).thenReturn(Optional.empty());

		var result = service.getRecruitments(request("REGISTRATION_DATE", "DESC"), true);

		verify(client, org.mockito.Mockito.timeout(1000)).fetchRecruitments(argThat(request ->
			request.resolvedPageNo() == 1 && request.resolvedNumOfRows() == 1000
		));
		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText())
			.isEqualTo("식품안전정보원 개방형 직위 공개 모집");
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
			progressStore
		);
		ObjectNode errorResponse = OBJECT_MAPPER.createObjectNode();
		errorResponse.put("resultCode", "500");
		errorResponse.put("resultMsg", "TEMPORARY_ERROR");

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(errorResponse);
		when(recruitmentRepository.findAll()).thenReturn(List.of());

		service.getRecruitments(request("REGISTRATION_DATE", "DESC"), true);

		verify(client, org.mockito.Mockito.timeout(1000).times(1)).fetchRecruitments(any(AlioRecruitmentListRequest.class));
		assertThat(progressStore.get().status()).isEqualTo("FAILED");
		assertThat(progressStore.get().failedPage()).isEqualTo(1);
		assertThat(progressStore.get().failureResponse().path("resultMsg").asText()).isEqualTo("TEMPORARY_ERROR");
	}

	private AlioRecruitmentService serviceWithCachedItems(ObjectNode response) {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentRepository recruitmentRepository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSyncStateRepository syncStateRepository = mock(AlioRecruitmentSyncStateRepository.class);

		when(recruitmentRepository.findAll()).thenReturn(toRecruitments(response));
		when(syncStateRepository.findById(any())).thenReturn(Optional.of(
			AlioRecruitmentSyncState.global(LocalDateTime.of(2026, 5, 16, 10, 30))
		));
		return new AlioRecruitmentService(
			client,
			recruitmentRepository,
			syncStateRepository,
			new AlioRecruitmentSyncProgressStore()
		);
	}

	private List<AlioRecruitment> toRecruitments(ObjectNode response) {
		ArrayNode items = (ArrayNode) response.at("/response/body/items/item");
		return Arrays.stream(OBJECT_MAPPER.convertValue(items, ObjectNode[].class))
			.map(item -> AlioRecruitment.from(item, LocalDateTime.now()))
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
		return recruitment(title, null, registrationDate, deadlineDate);
	}

	private ObjectNode recruitment(String title, String institution, String registrationDate, String deadlineDate) {
		ObjectNode node = OBJECT_MAPPER.createObjectNode();
		node.put("recrutPbancTtl", title);
		if (institution != null) {
			node.put("pblntInstNm", institution);
		}
		node.put("pbancBgngYmd", registrationDate);
		node.put("pbancEndYmd", deadlineDate);
		return node;
	}
}
