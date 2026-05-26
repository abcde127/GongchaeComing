package com.gongchae.gongchae_coming.alio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gongchae.gongchae_coming.alio.client.AlioPublicInstitutionClient;
import com.gongchae.gongchae_coming.alio.domain.PublicInstitution;
import com.gongchae.gongchae_coming.alio.repository.PublicInstitutionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PublicInstitutionServiceTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final AlioPublicInstitutionClient client = mock(AlioPublicInstitutionClient.class);
	private final PublicInstitutionRepository repository = mock(PublicInstitutionRepository.class);
	private final PublicInstitutionService service = new PublicInstitutionService(client, repository);

	@Test
	void synchronizePublicInstitutionsFetchesNextPageWhenTotalCountExceedsPageSize() throws Exception {
		when(client.fetchPublicInstitutions(1, 400)).thenReturn(response(401, "C0001", "88관광개발(주)"));
		when(client.fetchPublicInstitutions(2, 400)).thenReturn(response(401, "C0002", "한국가스공사"));

		int syncedCount = service.synchronizePublicInstitutions();

		ArgumentCaptor<List<PublicInstitution>> captor = ArgumentCaptor.captor();
		verify(repository).saveAll(captor.capture());
		assertThat(syncedCount).isEqualTo(2);
		assertThat(captor.getValue())
			.extracting(PublicInstitution::getInstCd)
			.containsExactly("C0001", "C0002");
		verify(client).fetchPublicInstitutions(1, 400);
		verify(client).fetchPublicInstitutions(2, 400);
	}

	@Test
	void getPublicInstitutionOptionsReturnsInstitutionsFromRepository() throws Exception {
		when(repository.findAllByOrderByInstNmAsc()).thenReturn(List.of(
			PublicInstitution.from(publicInstitution("C0001", "88관광개발(주)"), java.time.LocalDateTime.now()),
			PublicInstitution.from(publicInstitution("C0002", "한국가스공사"), java.time.LocalDateTime.now())
		));

		var options = service.getPublicInstitutionOptions();

		assertThat(options)
			.extracting("detailCode", "detailName")
			.containsExactly(
				org.assertj.core.groups.Tuple.tuple("C0001", "88관광개발(주)"),
				org.assertj.core.groups.Tuple.tuple("C0002", "한국가스공사")
			);
	}

	private JsonNode publicInstitution(String instCd, String instNm) throws Exception {
		return OBJECT_MAPPER.readTree("""
			{
				"instCd": "%s",
				"instNm": "%s"
			}
			""".formatted(instCd, instNm));
	}

	private JsonNode response(int totalCount, String instCd, String instNm) throws Exception {
		return OBJECT_MAPPER.readTree("""
			{
				"resultCode": 200,
				"resultMsg": "성공했습니다.",
				"totalCount": %d,
				"result": [
					{
						"instCd": "%s",
						"pbadmsStdInstCd": "B550402",
						"instNm": "%s",
						"sprvsnInstCd": "A1007",
						"sprvsnInstNm": "국가보훈부",
						"instType": "A2005",
						"instTypeNm": "기타공공기관",
						"instClsf": "99",
						"instClsfNm": "기타"
					}
				]
			}
			""".formatted(totalCount, instCd, instNm));
	}
}
