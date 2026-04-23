package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.dto.AlioFilterOptionResponse;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlioRecruitmentService {

	private static final List<AlioFilterOptionResponse> NCS_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("R600001", "사업관리"),
		new AlioFilterOptionResponse("R600002", "경영.회계.사무"),
		new AlioFilterOptionResponse("R600003", "금융.보험"),
		new AlioFilterOptionResponse("R600004", "교육.자연.사회과학"),
		new AlioFilterOptionResponse("R600005", "법률.경찰.소방.교도.국방"),
		new AlioFilterOptionResponse("R600006", "보건.의료"),
		new AlioFilterOptionResponse("R600007", "사회복지.종교"),
		new AlioFilterOptionResponse("R600008", "문화.예술.디자인.방송"),
		new AlioFilterOptionResponse("R600009", "운전.운송"),
		new AlioFilterOptionResponse("R600010", "영업판매"),
		new AlioFilterOptionResponse("R600011", "경비.청소"),
		new AlioFilterOptionResponse("R600012", "이용.숙박.여행.오락.스포츠"),
		new AlioFilterOptionResponse("R600013", "음식서비스"),
		new AlioFilterOptionResponse("R600014", "건설"),
		new AlioFilterOptionResponse("R600015", "기계"),
		new AlioFilterOptionResponse("R600016", "재료"),
		new AlioFilterOptionResponse("R600017", "화학"),
		new AlioFilterOptionResponse("R600018", "섬유.의복"),
		new AlioFilterOptionResponse("R600019", "전기.전자"),
		new AlioFilterOptionResponse("R600020", "정보통신"),
		new AlioFilterOptionResponse("R600021", "식품가공"),
		new AlioFilterOptionResponse("R600022", "인쇄.목재.가구.공예"),
		new AlioFilterOptionResponse("R600023", "환경.에너지.안전"),
		new AlioFilterOptionResponse("R600024", "농림어업"),
		new AlioFilterOptionResponse("R600025", "연구")
	);

	private static final List<AlioFilterOptionResponse> WORK_REGION_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("R3010", "서울특별시"),
		new AlioFilterOptionResponse("R3011", "인천광역시"),
		new AlioFilterOptionResponse("R3012", "대전광역시"),
		new AlioFilterOptionResponse("R3013", "대구광역시"),
		new AlioFilterOptionResponse("R3014", "부산광역시"),
		new AlioFilterOptionResponse("R3015", "광주광역시"),
		new AlioFilterOptionResponse("R3016", "울산광역시"),
		new AlioFilterOptionResponse("R3017", "경기도"),
		new AlioFilterOptionResponse("R3018", "강원도"),
		new AlioFilterOptionResponse("R3019", "충청남도"),
		new AlioFilterOptionResponse("R3020", "충청북도"),
		new AlioFilterOptionResponse("R3021", "경상북도"),
		new AlioFilterOptionResponse("R3022", "경상남도"),
		new AlioFilterOptionResponse("R3023", "전라남도"),
		new AlioFilterOptionResponse("R3024", "전라북도"),
		new AlioFilterOptionResponse("R3025", "제주특별자치도"),
		new AlioFilterOptionResponse("R3026", "세종특별자치시"),
		new AlioFilterOptionResponse("R3030", "해외")
	);

	private static final List<AlioFilterOptionResponse> HIRE_TYPE_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("R1010", "정규직"),
		new AlioFilterOptionResponse("R1020", "계약직"),
		new AlioFilterOptionResponse("R1030", "무기계약직"),
		new AlioFilterOptionResponse("R1040", "비정규직"),
		new AlioFilterOptionResponse("R1050", "청년인턴"),
		new AlioFilterOptionResponse("R1060", "청년인턴(체험형)"),
		new AlioFilterOptionResponse("R1070", "청년인턴(채용형)")
	);

	private static final List<AlioFilterOptionResponse> INSTITUTION_TYPE_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("A2001", "공기업(시장형)"),
		new AlioFilterOptionResponse("A2002", "공기업(준시장형)"),
		new AlioFilterOptionResponse("A2003", "준정부기관(기금관리형)"),
		new AlioFilterOptionResponse("A2004", "준정부기관(위탁집행형)"),
		new AlioFilterOptionResponse("A2005", "기타공공기관")
	);

	private static final List<AlioFilterOptionResponse> SORT_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("REGISTRATION_DATE", "등록일순"),
		new AlioFilterOptionResponse("DEADLINE_DATE", "마감일순")
	);

	private static final List<AlioFilterOptionResponse> SORT_DIRECTION_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("DESC", "내림차순"),
		new AlioFilterOptionResponse("ASC", "오름차순")
	);

	private static final List<AlioFilterOptionResponse> PAGE_SIZE_FILTER_OPTIONS = List.of(
		new AlioFilterOptionResponse("10", "10개씩 보기"),
		new AlioFilterOptionResponse("20", "20개씩 보기"),
		new AlioFilterOptionResponse("30", "30개씩 보기"),
		new AlioFilterOptionResponse("50", "50개씩 보기")
	);

	private final AlioRecruitmentClient alioRecruitmentClient;

	public JsonNode getRecruitments(AlioRecruitmentListRequest request) {
		JsonNode response = alioRecruitmentClient.fetchRecruitments(request);
		sortRecruitmentItems(response, request.resolvedSortBy(), request.resolvedSortDirection());
		return response;
	}

	public List<AlioFilterOptionResponse> getNcsFilterOptions() {
		return NCS_FILTER_OPTIONS;
	}

	public List<AlioFilterOptionResponse> getWorkRegionFilterOptions() {
		return WORK_REGION_FILTER_OPTIONS;
	}

	public List<AlioFilterOptionResponse> getHireTypeFilterOptions() {
		return HIRE_TYPE_FILTER_OPTIONS;
	}

	public List<AlioFilterOptionResponse> getInstitutionTypeFilterOptions() {
		return INSTITUTION_TYPE_FILTER_OPTIONS;
	}

	public List<AlioFilterOptionResponse> getSortFilterOptions() {
		return SORT_FILTER_OPTIONS;
	}

	public List<AlioFilterOptionResponse> getSortDirectionFilterOptions() {
		return SORT_DIRECTION_FILTER_OPTIONS;
	}

	public List<AlioFilterOptionResponse> getPageSizeFilterOptions() {
		return PAGE_SIZE_FILTER_OPTIONS;
	}

	private void sortRecruitmentItems(JsonNode response, String sortBy, String sortDirection) {
		ArrayNode items = findRecruitmentItems(response);
		if (items == null || items.size() < 2) {
			return;
		}

		List<JsonNode> sortedItems = new ArrayList<>();
		items.forEach(sortedItems::add);

		Comparator<LocalDate> dateComparator = "ASC".equals(sortDirection)
			? Comparator.naturalOrder()
			: Comparator.reverseOrder();

		Comparator<JsonNode> comparator = Comparator
			.comparing((JsonNode item) -> extractSortDate(item, sortBy), Comparator.nullsLast(dateComparator))
			.thenComparing(item -> extractSortDate(item, "REGISTRATION_DATE"), Comparator.nullsLast(dateComparator))
			.thenComparing(item -> item.path("recrutPbancTtl").asText(""));

		sortedItems.sort(comparator);
		items.removeAll();
		items.addAll(sortedItems);
	}

	private ArrayNode findRecruitmentItems(JsonNode response) {
		JsonNode responseNode = response.path("response");
		JsonNode bodyNode = responseNode.path("body");
		JsonNode itemsNode = bodyNode.path("items");

		if (itemsNode.isArray()) {
			return (ArrayNode) itemsNode;
		}

		JsonNode itemNode = itemsNode.path("item");
		if (itemNode.isArray()) {
			return (ArrayNode) itemNode;
		}

		JsonNode bodyItemNode = bodyNode.path("item");
		if (bodyItemNode.isArray()) {
			return (ArrayNode) bodyItemNode;
		}

		return null;
	}

	private LocalDate extractSortDate(JsonNode item, String sortBy) {
		if ("DEADLINE_DATE".equals(sortBy)) {
			LocalDate endDate = parseDate(item, "pbancEndYmd");
			if (endDate != null) {
				return endDate;
			}

			return parseDate(item, "aplyEndYmd", "endDate");
		}

		LocalDate registrationDate = parseDate(item, "pbancBgngYmd");
		if (registrationDate != null) {
			return registrationDate;
		}

		return parseDate(item, "pbancRgtrYmd", "regDt", "frstRegDt", "registrationDate");
	}

	private LocalDate parseDate(JsonNode item, String... fieldNames) {
		for (String fieldName : fieldNames) {
			String value = item.path(fieldName).asText(null);
			if (!StringUtils.hasText(value)) {
				continue;
			}

			try {
				return LocalDate.parse(value.trim());
			} catch (DateTimeParseException ignored) {
				// Ignore non-ISO date values and continue with fallback fields.
			}
		}

		return null;
	}
}
