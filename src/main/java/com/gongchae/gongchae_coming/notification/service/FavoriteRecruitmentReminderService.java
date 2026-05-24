package com.gongchae.gongchae_coming.notification.service;

import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentResponse;
import com.gongchae.gongchae_coming.favorite.repository.FavoriteRecruitmentRepository;
import com.gongchae.gongchae_coming.kakao.client.KakaoMessageClient;
import com.gongchae.gongchae_coming.kakao.client.KakaoTokenClient;
import com.gongchae.gongchae_coming.kakao.client.KakaoTokenClient.KakaoRefreshTokenResponse;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import com.gongchae.gongchae_coming.kakao.service.KakaoFavoriteReminderTemplateBuilder;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import com.gongchae.gongchae_coming.notification.domain.NotificationType;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
public class FavoriteRecruitmentReminderService {

	private final MemberRepository memberRepository;
	private final FavoriteRecruitmentRepository favoriteRecruitmentRepository;
	private final KakaoFavoriteReminderTemplateBuilder templateBuilder;
	private final KakaoMessageClient kakaoMessageClient;
	private final KakaoTokenClient kakaoTokenClient;
	private final NotificationHistoryService notificationHistoryService;

	@Transactional
	public void sendDueReminders(LocalTime now) {
		LocalTime reminderTime = now.truncatedTo(ChronoUnit.MINUTES);
		memberRepository.findFavoriteReminderTargets(reminderTime)
			.forEach(member -> {
				try {
					sendReminder(member);
				} catch (Exception exception) {
					recordFailureHistory(member, exception);
					log.warn("Failed to send favorite reminder. memberId={}", member.getId(), exception);
				}
			});
	}

	private void sendReminder(Member member) {
		List<FavoriteRecruitmentResponse> favorites = favoriteRecruitmentRepository
			.findByMemberIdOrderByCreatedAtDescIdDesc(member.getId())
			.stream()
			.map(favoriteRecruitment -> FavoriteRecruitmentResponse.from(favoriteRecruitment, false))
			.toList();
		KakaoTemplateObject templateObject = templateBuilder.buildTemplateObject(favorites, member.getFavoriteReminderTime());

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

	private void recordSuccessHistory(Member member) {
		try {
			notificationHistoryService.recordSuccess(member, NotificationType.FAVORITE_RECRUITMENT_REMINDER);
		} catch (Exception exception) {
			log.warn("Failed to record favorite reminder success history. memberId={}", member.getId(), exception);
		}
	}

	private void recordFailureHistory(Member member, Exception sendFailure) {
		try {
			notificationHistoryService.recordFailure(member, NotificationType.FAVORITE_RECRUITMENT_REMINDER, sendFailure);
		} catch (Exception exception) {
			log.warn("Failed to record favorite reminder failure history. memberId={}", member.getId(), exception);
		}
	}
}
