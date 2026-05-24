package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlioRecruitmentSeedImporter {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final AlioRecruitmentService alioRecruitmentService;

	@Value("${app.alio.recruitment-seed-location:classpath:seed/alio-recruitments.json}")
	private Resource seedResource;

	public int importSeedRecruitments() {
		if (!seedResource.exists()) {
			return 0;
		}

		try {
			JsonNode seed = OBJECT_MAPPER.readTree(seedResource.getInputStream());
			List<JsonNode> items = extractItems(seed);
			if (items.isEmpty()) {
				return 0;
			}
			alioRecruitmentService.importRecruitments(items, resolveFetchedAt(seed));
			log.info("Imported {} ALIO recruitment seed items.", items.size());
			return items.size();
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to read ALIO recruitment seed file.", exception);
		}
	}

	private List<JsonNode> extractItems(JsonNode seed) {
		JsonNode items = findItems(seed);
		if (items == null || !items.isArray()) {
			return List.of();
		}

		List<JsonNode> result = new ArrayList<>();
		items.forEach(result::add);
		return result;
	}

	private JsonNode findItems(JsonNode seed) {
		if (seed.isArray()) {
			return seed;
		}
		if (seed.path("items").isArray()) {
			return seed.path("items");
		}
		if (seed.path("result").isArray()) {
			return seed.path("result");
		}
		JsonNode alioItems = seed.at("/response/body/items/item");
		return alioItems.isMissingNode() ? null : alioItems;
	}

	private LocalDateTime resolveFetchedAt(JsonNode seed) {
		String fetchedAt = seed.path("fetchedAt").asText(null);
		if (fetchedAt == null || fetchedAt.isBlank()) {
			return LocalDateTime.now();
		}
		return LocalDateTime.parse(fetchedAt);
	}
}
