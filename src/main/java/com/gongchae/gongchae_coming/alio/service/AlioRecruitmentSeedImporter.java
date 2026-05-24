package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
			alioRecruitmentService.importRecruitments(items, LocalDateTime.now());
			log.info("Imported {} ALIO recruitment seed items.", items.size());
			return items.size();
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to read ALIO recruitment seed file.", exception);
		}
	}

	private List<JsonNode> extractItems(JsonNode seed) {
		JsonNode items = findItems(seed);
		if (items == null) {
			return List.of();
		}
		if (items.isObject()) {
			return extractKeyedItems(items);
		}
		if (!items.isArray()) {
			return List.of();
		}

		List<JsonNode> result = new ArrayList<>();
		items.forEach(result::add);
		return result;
	}

	private List<JsonNode> extractKeyedItems(JsonNode items) {
		List<JsonNode> result = new ArrayList<>();
		Iterator<Map.Entry<String, JsonNode>> fields = items.properties().iterator();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> field = fields.next();
			JsonNode item = field.getValue();
			if (!item.isObject()) {
				continue;
			}
			ObjectNode objectItem = ((ObjectNode) item).deepCopy();
			if (objectItem.path("recrutPblntSn").isMissingNode()) {
				objectItem.put("recrutPblntSn", field.getKey());
			}
			result.add(objectItem);
		}
		return result;
	}

	private JsonNode findItems(JsonNode seed) {
		if (seed.isArray()) {
			return seed;
		}
		if (seed.path("items").isArray()) {
			return seed.path("items");
		}
		if (seed.path("items").isObject()) {
			return seed.path("items");
		}
		if (seed.path("result").isArray()) {
			return seed.path("result");
		}
		JsonNode alioItems = seed.at("/response/body/items/item");
		return alioItems.isMissingNode() ? null : alioItems;
	}

}
