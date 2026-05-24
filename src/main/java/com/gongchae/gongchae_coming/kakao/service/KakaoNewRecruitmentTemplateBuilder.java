package com.gongchae.gongchae_coming.kakao.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.kakao.dto.KakaoButton;
import com.gongchae.gongchae_coming.kakao.dto.KakaoLink;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTextTemplateObject;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KakaoNewRecruitmentTemplateBuilder {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final int MAX_DISPLAY_COUNT = 2;
	private static final int MAX_TITLE_LENGTH = 10;
	private static final String RECRUITMENTS_PATH = "/recruitments";
	private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
	private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

	private final String webBaseUrl;

	@Autowired
	public KakaoNewRecruitmentTemplateBuilder(
		@Value("${app.web-base-url:http://localhost:8080}") String webBaseUrl
	) {
		this.webBaseUrl = normalizeBaseUrl(webBaseUrl);
	}

	public KakaoTemplateObject buildTemplateObject(List<AlioRecruitment> recruitments) {
		if (recruitments == null || recruitments.isEmpty()) {
			throw new IllegalArgumentException("new recruitments must not be empty");
		}

		List<AlioRecruitment> sortedRecruitments = recruitments.stream()
			.sorted(Comparator
				.comparing(this::extractEndDate, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(AlioRecruitment::getSourceRecruitmentId))
			.toList();
		KakaoLink link = createLink(recruitmentsUrl());

		return new KakaoTextTemplateObject(
			"text",
			createText(sortedRecruitments),
			link,
			List.of(new KakaoButton("공고 확인하기", link))
		);
	}

	private String createText(List<AlioRecruitment> recruitments) {
		StringBuilder text = new StringBuilder()
			.append("[공채왔어요] 신규공고 알림 🔔\n")
			.append("맞춤공고로 설정한 조건에 맞는 신규 채용공고 ")
			.append(recruitments.size())
			.append("건이 등록됐어요!\n\n");

		recruitments.stream()
			.limit(MAX_DISPLAY_COUNT)
			.forEach(recruitment -> text
				.append("💌 ")
				.append(shortenTitle(extractTitle(recruitment)))
				.append(" (~ ")
				.append(formatEndDate(recruitment))
				.append(")\n"));

		int remainingCount = recruitments.size() - MAX_DISPLAY_COUNT;
		if (remainingCount > 0) {
			text.append("\n외 ")
				.append(remainingCount)
				.append("개의 공고를 더 확인해보세요 👇");
		} else {
			text.append("\n확인해보세요 👇");
		}

		return text.toString();
	}

	private String extractTitle(AlioRecruitment recruitment) {
		ObjectNode item = toObjectNode(recruitment);
		String title = item.path("recrutPbancTtl").asText(null);
		return StringUtils.hasText(title) ? title.trim() : "제목 정보 없음";
	}

	private String shortenTitle(String title) {
		if (title.length() <= MAX_TITLE_LENGTH) {
			return title;
		}
		return title.substring(0, MAX_TITLE_LENGTH - 1).stripTrailing() + "…";
	}

	private String formatEndDate(AlioRecruitment recruitment) {
		LocalDate endDate = extractEndDate(recruitment);
		if (endDate == null) {
			return "마감일 미정";
		}
		return endDate.format(DISPLAY_DATE_FORMATTER);
	}

	private LocalDate extractEndDate(AlioRecruitment recruitment) {
		ObjectNode item = toObjectNode(recruitment);
		return parseDate(item.path("pbancEndYmd").asText(null));
	}

	private ObjectNode toObjectNode(AlioRecruitment recruitment) {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		recruitment.writeTo(item);
		return item;
	}

	private LocalDate parseDate(String... values) {
		for (String value : values) {
			if (!StringUtils.hasText(value)) {
				continue;
			}
			try {
				String trimmedValue = value.trim();
				if (trimmedValue.matches("^\\d{8}$")) {
					return LocalDate.parse(trimmedValue, BASIC_DATE_FORMATTER);
				}
				return LocalDate.parse(trimmedValue);
			} catch (DateTimeParseException ignored) {
				// Try the next date field when ALIO sends a non-ISO value.
			}
		}
		return null;
	}

	private KakaoLink createLink(String url) {
		return new KakaoLink(url, url);
	}

	private String recruitmentsUrl() {
		return webBaseUrl + RECRUITMENTS_PATH;
	}

	private String normalizeBaseUrl(String value) {
		String baseUrl = StringUtils.hasText(value) ? value.trim() : "http://localhost:8080";
		while (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl;
	}
}
