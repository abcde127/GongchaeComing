package com.gongchae.gongchae_coming.alio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.client.AlioRecruitmentClient;
import com.gongchae.gongchae_coming.alio.dto.AlioFilterOptionResponse;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

	private final AlioRecruitmentClient alioRecruitmentClient;

	public JsonNode getRecruitments(AlioRecruitmentListRequest request) {
		return alioRecruitmentClient.fetchRecruitments(request);
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
}
