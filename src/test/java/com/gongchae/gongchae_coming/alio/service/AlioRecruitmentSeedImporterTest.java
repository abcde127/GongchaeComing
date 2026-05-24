package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AlioRecruitmentSeedImporterTest {

	@Test
	void importSeedRecruitmentsImportsItemsFromConfiguredJsonResource() {
		AlioRecruitmentService service = mock(AlioRecruitmentService.class);
		AlioRecruitmentSeedImporter importer = new AlioRecruitmentSeedImporter(service);
		setSeedResource(importer, """
			{
			  "fetchedAt": "2026-05-24T10:30:00",
			  "items": [
			    {
			      "recrutPblntSn": 300658,
			      "recrutPbancTtl": "식품안전정보원 개방형 직위 공개 모집",
			      "pbancBgngYmd": "20260515",
			      "pbancEndYmd": "20260601"
			    }
			  ]
			}
			""");

		int importedCount = importer.importSeedRecruitments();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<JsonNode>> itemsCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<LocalDateTime> fetchedAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(service).importRecruitments(itemsCaptor.capture(), fetchedAtCaptor.capture());
		assertThat(importedCount).isEqualTo(1);
		assertThat(itemsCaptor.getValue()).hasSize(1);
		assertThat(itemsCaptor.getValue().get(0).path("recrutPbancTtl").asText())
			.isEqualTo("식품안전정보원 개방형 직위 공개 모집");
		assertThat(fetchedAtCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 5, 24, 10, 30));
	}

	@Test
	void importSeedRecruitmentsSkipsEmptySeedFile() {
		AlioRecruitmentService service = mock(AlioRecruitmentService.class);
		AlioRecruitmentSeedImporter importer = new AlioRecruitmentSeedImporter(service);
		setSeedResource(importer, """
			{
			  "items": []
			}
			""");

		int importedCount = importer.importSeedRecruitments();

		assertThat(importedCount).isZero();
		verifyNoInteractions(service);
	}

	private void setSeedResource(AlioRecruitmentSeedImporter importer, String content) {
		Resource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
		ReflectionTestUtils.setField(importer, "seedResource", resource);
	}
}
