package com.gongchae.gongchae_coming.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import com.gongchae.gongchae_coming.notification.domain.NotificationType;
import com.gongchae.gongchae_coming.notification.dto.NotificationHistoryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationHistoryServiceTest {

	@Autowired
	private NotificationHistoryService notificationHistoryService;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	void getMyHistoriesReturnsRecentNotificationResults() {
		Member member = memberRepository.save(Member.create("history-user@example.com", "historygongchae", "password1"));

		notificationHistoryService.recordSuccess(member, NotificationType.FAVORITE_RECRUITMENT_REMINDER);
		notificationHistoryService.recordFailure(
			member,
			NotificationType.NEW_RECRUITMENT,
			new IllegalStateException("Kakao API rejected message")
		);

		List<NotificationHistoryResponse> histories = notificationHistoryService.getMyHistories("history-user@example.com");

		assertThat(histories).hasSize(2);
		assertThat(histories.get(0).type()).isEqualTo("NEW_RECRUITMENT");
		assertThat(histories.get(0).typeLabel()).isEqualTo("신규공고 알림");
		assertThat(histories.get(0).status()).isEqualTo("FAILURE");
		assertThat(histories.get(0).statusLabel()).isEqualTo("실패");
		assertThat(histories.get(0).failureReason()).isEqualTo("Kakao API rejected message");
		assertThat(histories.get(1).type()).isEqualTo("FAVORITE_RECRUITMENT_REMINDER");
		assertThat(histories.get(1).status()).isEqualTo("SUCCESS");
		assertThat(histories.get(1).failureReason()).isNull();
	}
}
