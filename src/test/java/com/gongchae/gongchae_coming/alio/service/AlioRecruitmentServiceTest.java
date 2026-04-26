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

	private AlioRecruitmentListRequest request(String sortBy) {
		return request(sortBy, null);
	}

	private AlioRecruitmentListRequest request(String sortBy, String sortDirection) {
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
		ObjectNode node = OBJECT_MAPPER.createObjectNode();
		node.put("recrutPbancTtl", title);
		node.put("pbancBgngYmd", registrationDate);
		node.put("pbancEndYmd", deadlineDate);
		return node;
	}
}
