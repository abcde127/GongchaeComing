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
	void getNcsFilterOptionsReturnsAlioNcsCodes() {
		var options = alioRecruitmentService.getNcsFilterOptions();

		assertThat(options).hasSize(25);
		assertThat(options.get(0).code()).isEqualTo("R600001");
		assertThat(options.get(0).name()).isEqualTo("사업관리");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("R600025");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("연구");
	}

	@Test
	void getWorkRegionFilterOptionsReturnsAlioWorkRegionCodes() {
		var options = alioRecruitmentService.getWorkRegionFilterOptions();

		assertThat(options).hasSize(18);
		assertThat(options.get(0).code()).isEqualTo("R3010");
		assertThat(options.get(0).name()).isEqualTo("서울특별시");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("R3030");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("해외");
	}

	@Test
	void getHireTypeFilterOptionsReturnsAlioHireTypeCodes() {
		var options = alioRecruitmentService.getHireTypeFilterOptions();

		assertThat(options).hasSize(7);
		assertThat(options.get(0).code()).isEqualTo("R1010");
		assertThat(options.get(0).name()).isEqualTo("정규직");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("R1070");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("청년인턴(채용형)");
	}

	@Test
	void getInstitutionTypeFilterOptionsReturnsInstitutionTypeCodes() {
		var options = alioRecruitmentService.getInstitutionTypeFilterOptions();

		assertThat(options).hasSize(5);
		assertThat(options.get(0).code()).isEqualTo("A2001");
		assertThat(options.get(0).name()).isEqualTo("공기업(시장형)");
		assertThat(options.get(options.size() - 1).code()).isEqualTo("A2005");
		assertThat(options.get(options.size() - 1).name()).isEqualTo("기타공공기관");
	}

	@Test
	void getSortFilterOptionsReturnsSupportedSortTypes() {
		var options = alioRecruitmentService.getSortFilterOptions();

		assertThat(options).hasSize(2);
		assertThat(options.get(0).code()).isEqualTo("REGISTRATION_DATE");
		assertThat(options.get(0).name()).isEqualTo("등록일순");
		assertThat(options.get(1).code()).isEqualTo("DEADLINE_DATE");
		assertThat(options.get(1).name()).isEqualTo("마감일순");
	}

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

	private AlioRecruitmentListRequest request(String sortBy) {
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
			sortBy,
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
		ObjectNode node = OBJECT_MAPPER.createObjectNode();
		node.put("recrutPbancTtl", title);
		node.put("pbancBgngYmd", registrationDate);
		node.put("pbancEndYmd", deadlineDate);
		return node;
	}
}
