package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlioRecruitmentSeedExporterTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@TempDir
	Path tempDir;

	@Test
	void exportSeedRecruitmentsWritesRepositoryItemsToJsonFile() throws Exception {
		AlioRecruitmentRepository repository = mock(AlioRecruitmentRepository.class);
		AlioRecruitmentSeedExporter exporter = new AlioRecruitmentSeedExporter(repository);
		Path seedPath = tempDir.resolve("alio-recruitments.json");
		ReflectionTestUtils.setField(exporter, "seedOutputPath", seedPath.toString());
		when(repository.findAll()).thenReturn(List.of(
			recruitment(100L, "이전 공고"),
			recruitment(101L, "신규 공고")
		));

		int exportedCount = exporter.exportSeedRecruitments();

		JsonNode seed = OBJECT_MAPPER.readTree(seedPath.toFile());
		assertThat(exportedCount).isEqualTo(2);
		assertThat(seed.path("items")).hasSize(2);
		assertThat(seed.at("/items/0/recrutPblntSn").asLong()).isEqualTo(101L);
		assertThat(seed.at("/items/0/recrutPbancTtl").asText()).isEqualTo("신규 공고");
		assertThat(seed.path("fetchedAt").isMissingNode()).isTrue();
		assertThat(seed.at("/items/0/fetchedAt").isMissingNode()).isTrue();
	}

	private AlioRecruitment recruitment(Long sequence, String title) {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		item.put("recrutPblntSn", sequence);
		item.put("recrutPbancTtl", title);
		item.put("pbancBgngYmd", "20260515");
		item.put("pbancEndYmd", "20260601");
		return AlioRecruitment.from(item, LocalDateTime.now());
	}
}
