package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlioRecruitmentServiceTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final AlioRecruitmentService alioRecruitmentService =
		new AlioRecruitmentService(mock(AlioRecruitmentClient.class));

	@Test
	void getRecruitmentsSortsItemsByRegistrationDateByDefault() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentService service = new AlioRecruitmentService(client);
		ObjectNode response = createResponse(
			recruitment("first", "2026-04-01", "2026-04-10"),
			recruitment("second", "2026-04-15", "2026-04-20")
		);

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(response);

		var result = service.getRecruitments(request(null));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("first");
	}

	@Test
	void getRecruitmentsSortsItemsByRegistrationDateAscendingWhenRequested() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentService service = new AlioRecruitmentService(client);
		ObjectNode response = createResponse(
			recruitment("first", "2026-04-01", "2026-04-10"),
			recruitment("second", "2026-04-15", "2026-04-20")
		);

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(response);

		var result = service.getRecruitments(request("REGISTRATION_DATE", "ASC"));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("first");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("second");
	}

	@Test
	void getRecruitmentsSortsItemsByDeadlineDateWhenRequested() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentService service = new AlioRecruitmentService(client);
		ObjectNode response = createResponse(
			recruitment("first", "2026-04-20", "2026-04-21"),
			recruitment("second", "2026-04-10", "2026-04-25")
		);

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(response);

		var result = service.getRecruitments(request("DEADLINE_DATE"));

		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("second");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("first");
	}

	@Test
	void getRecruitmentsAttachesDebugInfoWhenAlioReturnsErrorPayload() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentService service = new AlioRecruitmentService(client);
		ObjectNode errorResponse = OBJECT_MAPPER.createObjectNode();
		errorResponse.put("resultCode", "6");
		errorResponse.put("resultMsgEng", "SERVER_UNKNOWN_ERROR");

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(errorResponse);
		when(client.buildRequestMethodForDebug()).thenReturn("GET");
		when(client.buildRequestUriForDebug(any(AlioRecruitmentListRequest.class)))
			.thenReturn("https://opendata.alio.go.kr/new/v1/recruit/list.do?recrutPbancTtl=nhis");

		var result = service.getRecruitments(request("REGISTRATION_DATE", null, "nhis"));

		assertThat(result.at("/_debug/alioRequestMethod").asText()).isEqualTo("GET");
		assertThat(result.at("/_debug/alioRequestUri").asText())
			.isEqualTo("https://opendata.alio.go.kr/new/v1/recruit/list.do?recrutPbancTtl=nhis");
		assertThat(result.at("/_debug/searchKeyword").asText()).isEqualTo("nhis");
		assertThat(result.at("/_debug/recrutPbancTtl").asText()).isEqualTo("nhis");
	}

	@Test
	void getRecruitmentsFiltersItemsBySearchKeywordAgainstTitleAndInstitution() {
		AlioRecruitmentClient client = mock(AlioRecruitmentClient.class);
		AlioRecruitmentService service = new AlioRecruitmentService(client);
		ObjectNode response = createResponse(
			recruitment("국민건강보험공단 체험형 인턴", "국민건강보험공단", "2026-04-01", "2026-04-10"),
			recruitment("한국전력공사 신입 채용", "한국전력공사", "2026-04-02", "2026-04-11"),
			recruitment("일반 행정 채용", "국민건강보험공단", "2026-04-03", "2026-04-12")
		);

		when(client.fetchRecruitments(any(AlioRecruitmentListRequest.class))).thenReturn(response);

		var result = service.getRecruitments(request("REGISTRATION_DATE", "DESC", "건강보험공단"));

		assertThat(result.at("/response/body/items/item")).hasSize(2);
		assertThat(result.at("/response/body/items/item/0/recrutPbancTtl").asText()).isEqualTo("일반 행정 채용");
		assertThat(result.at("/response/body/items/item/1/recrutPbancTtl").asText()).isEqualTo("국민건강보험공단 체험형 인턴");
		assertThat(result.at("/totalCount").asInt()).isEqualTo(2);
		assertThat(result.at("/response/body/totalCount").asInt()).isEqualTo(2);
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
