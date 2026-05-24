package com.gongchae.gongchae_coming.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.kakao.client.KakaoMessageClient;
import com.gongchae.gongchae_coming.kakao.client.KakaoTokenClient;
import com.gongchae.gongchae_coming.kakao.dto.KakaoTemplateObject;
import com.gongchae.gongchae_coming.kakao.service.KakaoNewRecruitmentTemplateBuilder;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import com.gongchae.gongchae_coming.notification.domain.NotificationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewRecruitmentNotificationServiceTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final MemberRepository memberRepository = mock(MemberRepository.class);
	private final KakaoMessageClient kakaoMessageClient = mock(KakaoMessageClient.class);
	private final NotificationHistoryService notificationHistoryService = mock(NotificationHistoryService.class);
	private final NewRecruitmentNotificationService notificationService = new NewRecruitmentNotificationService(
		memberRepository,
		new KakaoNewRecruitmentTemplateBuilder("https://gongchae.example.com"),
		kakaoMessageClient,
		mock(KakaoTokenClient.class),
		notificationHistoryService
	);

	@Test
	void sendsAllNewRecruitmentsWhenMemberHasNoJobPreference() {
		Member member = kakaoLinkedMember("all@example.com");
		when(memberRepository.findNewRecruitmentNotificationTargets()).thenReturn(List.of(member));

		notificationService.sendNewRecruitmentNotifications(List.of(
			recruitment("한국전력공사 채용", "C001", "R3010", "R2010", "R1010", "R600001"),
			recruitment("국민건강보험공단 채용", "C002", "R3011", "R2020", "R1020", "R600002")
		));

		verify(kakaoMessageClient).sendDefaultMessage(eq("access-token"), any(KakaoTemplateObject.class));
		verify(notificationHistoryService).recordSuccess(member, NotificationType.NEW_RECRUITMENT);
	}

	@Test
	void sendsOnlyWhenNewRecruitmentsMatchMemberJobPreference() {
		Member matchedMember = kakaoLinkedMember("matched@example.com");
		matchedMember.updateJobPreference(null, null, null, "R3010", null, null, null);
		Member unmatchedMember = kakaoLinkedMember("unmatched@example.com");
		unmatchedMember.updateJobPreference(null, null, null, "R3026", null, null, null);
		when(memberRepository.findNewRecruitmentNotificationTargets()).thenReturn(List.of(matchedMember, unmatchedMember));

		notificationService.sendNewRecruitmentNotifications(List.of(
			recruitment("한국전력공사 채용", "C001", "R3010", "R2010", "R1010", "R600001")
		));

		verify(kakaoMessageClient).sendDefaultMessage(eq("access-token"), any(KakaoTemplateObject.class));
		verify(notificationHistoryService).recordSuccess(matchedMember, NotificationType.NEW_RECRUITMENT);
		verify(notificationHistoryService, never()).recordSuccess(unmatchedMember, NotificationType.NEW_RECRUITMENT);
	}

	private Member kakaoLinkedMember(String email) {
		Member member = Member.create(email, email, "password");
		member.updateKakaoToken("access-token", 3600, "refresh-token", 3600);
		return member;
	}

	private AlioRecruitment recruitment(
		String title,
		String companyCode,
		String region,
		String category,
		String hireType,
		String ncsCode
	) {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		item.put("recrutPblntSn", Math.abs(title.hashCode()));
		item.put("recrutPbancTtl", title);
		item.put("pblntInstCd", companyCode);
		item.put("workRgnLst", region);
		item.put("recrutSe", category);
		item.put("hireTypeLst", hireType);
		item.put("ncsCdLst", ncsCode);
		item.put("pbancBgngYmd", LocalDate.now().minusDays(1).toString());
		item.put("pbancEndYmd", LocalDate.now().plusDays(10).toString());
		return AlioRecruitment.from(item, LocalDateTime.now());
	}
}
