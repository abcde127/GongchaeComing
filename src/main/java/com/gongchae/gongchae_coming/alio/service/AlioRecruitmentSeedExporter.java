package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.repository.AlioRecruitmentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlioRecruitmentSeedExporter {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT);

	private final AlioRecruitmentRepository alioRecruitmentRepository;

	@Value("${app.alio.recruitment-seed-output-path:src/main/resources/seed/alio-recruitments.json}")
	private String seedOutputPath;

	@Transactional(readOnly = true)
	public int exportSeedRecruitments() {
		if (!StringUtils.hasText(seedOutputPath)) {
			return 0;
		}

		List<AlioRecruitment> recruitments = alioRecruitmentRepository.findAll()
			.stream()
			.sorted(Comparator
				.comparing(AlioRecruitment::getRecrutPblntSn, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(AlioRecruitment::getSourceRecruitmentId))
			.toList();
		writeSeedFile(recruitments);
		log.info("Exported {} ALIO recruitment seed items.", recruitments.size());
		return recruitments.size();
	}

	private void writeSeedFile(List<AlioRecruitment> recruitments) {
		Path outputPath = Path.of(seedOutputPath).toAbsolutePath().normalize();
		Path tempPath = outputPath.resolveSibling(outputPath.getFileName() + ".tmp");
		try {
			Files.createDirectories(outputPath.getParent());
			OBJECT_MAPPER.writeValue(tempPath.toFile(), buildSeedJson(recruitments));
			Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to write ALIO recruitment seed file.", exception);
		}
	}

	private ObjectNode buildSeedJson(List<AlioRecruitment> recruitments) {
		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		ArrayNode items = root.putArray("items");
		recruitments.forEach(recruitment -> {
			ObjectNode item = OBJECT_MAPPER.createObjectNode();
			recruitment.writeTo(item);
			items.add(item);
		});
		return root;
	}
}
