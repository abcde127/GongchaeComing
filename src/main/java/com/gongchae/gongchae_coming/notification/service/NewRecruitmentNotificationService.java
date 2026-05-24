package com.gongchae.gongchae_coming.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.kakao.client.KakaoMessageClient;
import com.gongchae.gongchae_coming.kakao.client.KakaoTokenClient;
import com.gongchae.gongchae_coming.kakao.client.KakaoTokenClient.KakaoRefreshTokenResponse;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import com.gongchae.gongchae_coming.kakao.service.KakaoNewRecruitmentTemplateBuilder;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import com.gongchae.gongchae_coming.notification.domain.NotificationType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewRecruitmentNotificationService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	private final MemberRepository memberRepository;
	private final KakaoNewRecruitmentTemplateBuilder templateBuilder;
	private final KakaoMessageClient kakaoMessageClient;
	private final KakaoTokenClient kakaoTokenClient;
	private final NotificationHistoryService notificationHistoryService;

	@Transactional
	public void sendNewRecruitmentNotifications(List<AlioRecruitment> newRecruitments) {
		if (newRecruitments == null || newRecruitments.isEmpty()) {
			return;
		}

		try {
			memberRepository.findNewRecruitmentNotificationTargets()
				.forEach(member -> {
					List<AlioRecruitment> matchedRecruitments = filterByMemberPreference(member, newRecruitments);
					if (matchedRecruitments.isEmpty()) {
						return;
					}

					try {
						sendNotification(member, matchedRecruitments);
					} catch (Exception exception) {
						recordFailureHistory(member, exception);
						log.warn("Failed to send new recruitment notification. memberId={}", member.getId(), exception);
					}
				});
		} catch (Exception exception) {
			log.warn("Failed to send new recruitment notifications.", exception);
		}
	}

	private void sendNotification(Member member, List<AlioRecruitment> recruitments) {
		KakaoTemplateObject templateObject = templateBuilder.buildTemplateObject(recruitments);

		try {
			kakaoMessageClient.sendDefaultMessage(member.getKakaoAccessToken(), templateObject);
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode() != HttpStatus.UNAUTHORIZED) {
				throw exception;
			}
			refreshTokenAndRetry(member, templateObject);
		}
		recordSuccessHistory(member);
	}

	private void refreshTokenAndRetry(Member member, KakaoTemplateObject templateObject) {
		KakaoRefreshTokenResponse tokenResponse = kakaoTokenClient.refreshAccessToken(member.getKakaoRefreshToken());
		if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
			throw new IllegalStateException("invalid Kakao refresh token response");
		}

		member.updateKakaoAccessToken(tokenResponse.accessToken(), tokenResponse.expiresIn());
		if (StringUtils.hasText(tokenResponse.refreshToken())) {
			member.updateKakaoRefreshToken(tokenResponse.refreshToken(), tokenResponse.refreshTokenExpiresIn());
		}

		kakaoMessageClient.sendDefaultMessage(member.getKakaoAccessToken(), templateObject);
	}

	private List<AlioRecruitment> filterByMemberPreference(Member member, List<AlioRecruitment> recruitments) {
		if (!hasJobPreference(member)) {
			return recruitments;
		}

		List<Predicate<ObjectNode>> predicates = buildPredicates(member);
		return recruitments.stream()
			.filter(recruitment -> {
				ObjectNode item = toObjectNode(recruitment);
				return predicates.stream().allMatch(predicate -> predicate.test(item));
			})
			.toList();
	}

	private boolean hasJobPreference(Member member) {
		return StringUtils.hasText(member.getPreferredSearchKeyword())
			|| !member.splitPreferredCompanies().isEmpty()
			|| !member.splitPreferredRecruitmentStatuses().isEmpty()
			|| !member.splitPreferredRegions().isEmpty()
			|| !member.splitPreferredCategories().isEmpty()
			|| !member.splitPreferredHireTypes().isEmpty()
			|| !member.splitPreferredNcsCodes().isEmpty();
	}

	private List<Predicate<ObjectNode>> buildPredicates(Member member) {
		List<Predicate<ObjectNode>> predicates = new ArrayList<>();

		if (StringUtils.hasText(member.getPreferredSearchKeyword())) {
			String normalizedKeyword = normalizeKeyword(member.getPreferredSearchKeyword());
			predicates.add(item -> {
				String title = normalizeKeyword(item.path("recrutPbancTtl").asText(""));
				String institution = normalizeKeyword(firstNonBlank(
					item.path("pblntInstNm").asText(""),
					item.path("instNm").asText("")
				));
				return title.contains(normalizedKeyword) || institution.contains(normalizedKeyword);
			});
		}

		addContainsAnyPredicate(predicates, member.splitPreferredCompanies(), "pblntInstCd", "pblntInstNm", "instNm");
		addContainsAnyPredicate(predicates, member.splitPreferredRegions(), "workRgnLst", "workRgnNmLst");
		addContainsAnyPredicate(predicates, member.splitPreferredCategories(), "recrutSe", "recrutSeNm");
		addContainsAnyPredicate(predicates, member.splitPreferredHireTypes(), "hireTypeLst", "hireTypeNmLst");
		addContainsAnyPredicate(predicates, member.splitPreferredNcsCodes(), "ncsCdLst", "ncsCdNmLst");
		addRecruitmentStatusPredicate(predicates, member.splitPreferredRecruitmentStatuses());

		return predicates;
	}

	private void addContainsAnyPredicate(
		List<Predicate<ObjectNode>> predicates,
		List<String> values,
		String... fieldNames
	) {
		if (values.isEmpty()) {
			return;
		}

		predicates.add(item -> values.stream().anyMatch(value -> {
			for (String fieldName : fieldNames) {
				if (item.path(fieldName).asText("").contains(value)) {
					return true;
				}
			}
			return false;
		}));
	}

	private void addRecruitmentStatusPredicate(List<Predicate<ObjectNode>> predicates, List<String> values) {
		if (values.isEmpty()) {
			return;
		}

		Set<String> statuses = values.stream()
			.filter(StringUtils::hasText)
			.map(String::trim)
			.collect(Collectors.toSet());
		predicates.add(item -> statuses.contains(resolveRecruitmentStatus(item)));
	}

	private String resolveRecruitmentStatus(ObjectNode item) {
		LocalDate today = LocalDate.now();
		LocalDate startDate = parseDate(item.path("pbancBgngYmd").asText(null), item.path("pbancRgtrYmd").asText(null));
		LocalDate endDate = parseDate(
			item.path("pbancEndYmd").asText(null),
			item.path("aplyEndYmd").asText(null)
		);
		if (startDate == null || endDate == null) {
			return null;
		}
		if (today.isBefore(startDate)) {
			return "scheduled";
		}
		if (today.isAfter(endDate)) {
			return "closed";
		}
		return "active";
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

	private String normalizeKeyword(String value) {
		return StringUtils.hasText(value)
			? value.replaceAll("\\s+", "").toLowerCase()
			: "";
	}

	private String firstNonBlank(String first, String second) {
		if (StringUtils.hasText(first)) {
			return first;
		}
		return second;
	}

	private void recordSuccessHistory(Member member) {
		try {
			notificationHistoryService.recordSuccess(member, NotificationType.NEW_RECRUITMENT);
		} catch (Exception exception) {
			log.warn("Failed to record new recruitment notification success history. memberId={}", member.getId(), exception);
		}
	}

	private void recordFailureHistory(Member member, Exception sendFailure) {
		try {
			notificationHistoryService.recordFailure(member, NotificationType.NEW_RECRUITMENT, sendFailure);
		} catch (Exception exception) {
			log.warn("Failed to record new recruitment notification failure history. memberId={}", member.getId(), exception);
		}
	}
}
