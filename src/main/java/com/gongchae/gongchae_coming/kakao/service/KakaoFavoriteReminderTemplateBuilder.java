package com.gongchae.gongchae_coming.kakao.service;

import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentResponse;
import com.gongchae.gongchae_coming.kakao.dto.KakaoButton;
import com.gongchae.gongchae_coming.kakao.dto.KakaoLink;
import com.gongchae.gongchae_coming.kakao.dto.KakaoListContent;
import com.gongchae.gongchae_coming.kakao.dto.KakaoListTemplateObject;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTextTemplateObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KakaoFavoriteReminderTemplateBuilder {

	private static final int MAX_CONTENTS_SIZE = 3;
	private static final int MIN_LIST_CONTENTS_SIZE = 2;
	private static final int MAX_TEXT_LENGTH = 200;
	private static final String FAVORITES_PATH = "/mypage?section=favorites";
	private static final String RECRUITMENTS_PATH = "/recruitments";
	private static final String RECRUITMENT_REDIRECT_PATH_PREFIX = "/recruitments/";
	private static final String RECRUITMENT_REDIRECT_PATH_SUFFIX = "/redirect";
	private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	private final String webBaseUrl;
	private final Clock clock;

	@Autowired
	public KakaoFavoriteReminderTemplateBuilder(
		@Value("${app.web-base-url:http://localhost:8080}") String webBaseUrl
	) {
		this(webBaseUrl, Clock.systemDefaultZone());
	}

	KakaoFavoriteReminderTemplateBuilder(String webBaseUrl, Clock clock) {
		this.webBaseUrl = normalizeBaseUrl(webBaseUrl);
		this.clock = clock;
	}

	public KakaoTemplateObject buildTemplateObject(List<FavoriteRecruitmentResponse> favorites) {
		return buildTemplateObject(favorites, null);
	}

	public KakaoTemplateObject buildTemplateObject(List<FavoriteRecruitmentResponse> favorites, java.time.LocalTime reminderTime) {
		List<FavoriteRecruitmentResponse> linkableFavorites = favorites.stream()
			.filter(favorite -> StringUtils.hasText(favorite.recruitmentUrl()))
			.toList();
		if (linkableFavorites.isEmpty()) {
			return createEmptyFavoriteTextTemplate(reminderTime);
		}

		List<FavoriteRecruitmentResponse> sortedFavorites = sortByDeadline(linkableFavorites);
		if (sortedFavorites.size() < MIN_LIST_CONTENTS_SIZE) {
			return createTextTemplate(favorites.size(), sortedFavorites.get(0));
		}

		return createListTemplate(favorites.size(), sortedFavorites);
	}

	private KakaoListTemplateObject createListTemplate(int totalCount, List<FavoriteRecruitmentResponse> favorites) {
		return new KakaoListTemplateObject(
			"list",
			"[공채왔어요 리마인드] - 총 " + totalCount + "개의 관심 공고가 등록되어 있습니다",
			createLink(settingsUrl()),
			createContents(favorites),
			List.of(new KakaoButton("관심공고 더보기", createLink(settingsUrl())))
		);
	}

	private KakaoTextTemplateObject createTextTemplate(int totalCount, FavoriteRecruitmentResponse favorite) {
		KakaoLink link = createLink(recruitmentRedirectUrl(favorite.sourceRecruitmentId()));
		return new KakaoTextTemplateObject(
			"text",
			limitText(
				"[공채왔어요 리마인드]\n"
					+ "💙 관심 공고를 확인해주세요 💙\n"
					+ "\n"
					+ "📌 " + normalizeTitle(favorite.recruitmentTitle()) + "\n"
					+ "🏢 " + normalizeInstitutionName(favorite.institutionName()) + "\n"
					+ "⏰ " + createRemainingDaysText(favorite.recruitmentEndDate()) + "\n"
					+ "\n"
					+ "놓치기 전에 지금 바로 확인해보세요!",
				MAX_TEXT_LENGTH
			),
			link,
			List.of(new KakaoButton("원문보기", link))
		);
	}

	private KakaoTextTemplateObject createEmptyFavoriteTextTemplate(java.time.LocalTime reminderTime) {
		KakaoLink link = createLink(recruitmentsUrl());
		return new KakaoTextTemplateObject(
			"text",
			limitText(
				"[공채왔어요 리마인드]\n"
					+ "\n"
					+ "아직 관심 공고를 등록하지 않았어요..🥺\n"
					+ "\n"
					+ "관심 공고를 등록하면 매일 "
					+ formatReminderTime(reminderTime)
					+ "마다 잊지 않게 알려드릴게요!",
				MAX_TEXT_LENGTH
			),
			link,
			List.of(new KakaoButton("관심공고 설정하러가기", link))
		);
	}

	private List<KakaoListContent> createContents(List<FavoriteRecruitmentResponse> favorites) {
		return favorites.stream()
			.limit(MAX_CONTENTS_SIZE)
				.map(favorite -> new KakaoListContent(
					normalizeTitle(favorite.recruitmentTitle()),
					createDescription(favorite),
					createLink(recruitmentRedirectUrl(favorite.sourceRecruitmentId()))
				))
			.toList();
	}

	private List<FavoriteRecruitmentResponse> sortByDeadline(List<FavoriteRecruitmentResponse> favorites) {
		return favorites.stream()
			.sorted(Comparator
				.comparing(
					(FavoriteRecruitmentResponse favorite) -> parseRecruitmentEndDate(favorite.recruitmentEndDate()),
					Comparator.nullsLast(Comparator.naturalOrder())
				)
				.thenComparing(FavoriteRecruitmentResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(FavoriteRecruitmentResponse::id, Comparator.nullsLast(Comparator.reverseOrder()))
			)
			.toList();
	}

	private String createDescription(FavoriteRecruitmentResponse favorite) {
		String institutionName = StringUtils.hasText(favorite.institutionName())
			? favorite.institutionName().trim()
			: "기관명 미정";
		return institutionName + " · " + createDdayText(favorite.recruitmentEndDate());
	}

	private String createDdayText(String recruitmentEndDate) {
		if (!StringUtils.hasText(recruitmentEndDate)) {
			return "마감일 미정";
		}

		LocalDate endDate = parseRecruitmentEndDate(recruitmentEndDate);
		if (endDate == null) {
			return "마감일 미정";
		}

		LocalDate today = LocalDate.now(clock);
		long days = ChronoUnit.DAYS.between(today, endDate);
		if (days == 0) {
			return "마감까지 0일";
		}
		return "마감까지 " + days + "일";
	}

	private String createRemainingDaysText(String recruitmentEndDate) {
		LocalDate endDate = parseRecruitmentEndDate(recruitmentEndDate);
		if (endDate == null) {
			return "마감일을 확인해주세요!";
		}

		long days = ChronoUnit.DAYS.between(LocalDate.now(clock), endDate);
		if (days == 0) {
			return "오늘 마감이에요!";
		}
		return "마감까지 " + days + "일 남았어요!";
	}

	private LocalDate parseRecruitmentEndDate(String recruitmentEndDate) {
		if (!StringUtils.hasText(recruitmentEndDate)) {
			return null;
		}

		String value = recruitmentEndDate.trim();
		try {
			if (value.matches("^\\d{8}$")) {
				return LocalDate.parse(value, BASIC_DATE_FORMATTER);
			}
			return LocalDate.parse(value);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}

	private KakaoLink createLink(String url) {
		return new KakaoLink(url, url);
	}

	private String settingsUrl() {
		return webBaseUrl + FAVORITES_PATH;
	}

	private String recruitmentsUrl() {
		return webBaseUrl + RECRUITMENTS_PATH;
	}

	private String recruitmentRedirectUrl(String sourceRecruitmentId) {
		if (!StringUtils.hasText(sourceRecruitmentId)) {
			throw new IllegalArgumentException("sourceRecruitmentId is required for recruitment redirect link");
		}
		return webBaseUrl
			+ RECRUITMENT_REDIRECT_PATH_PREFIX
			+ URLEncoder.encode(sourceRecruitmentId.trim(), StandardCharsets.UTF_8).replace("+", "%20")
			+ RECRUITMENT_REDIRECT_PATH_SUFFIX;
	}

	private String normalizeBaseUrl(String value) {
		String baseUrl = StringUtils.hasText(value) ? value.trim() : "http://localhost:8080";
		while (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl;
	}

	private String normalizeTitle(String value) {
		return StringUtils.hasText(value) ? value.trim() : "제목 정보 없음";
	}

	private String normalizeInstitutionName(String value) {
		return StringUtils.hasText(value) ? value.trim() : "기관명 미정";
	}

	private String limitText(String value, int maxLength) {
		return value.length() <= maxLength ? value : value.substring(0, maxLength);
	}

	private String formatReminderTime(java.time.LocalTime reminderTime) {
		java.time.LocalTime resolvedTime = reminderTime == null ? java.time.LocalTime.of(9, 0) : reminderTime;
		return String.format("%02d시 %02d분", resolvedTime.getHour(), resolvedTime.getMinute());
	}
}
