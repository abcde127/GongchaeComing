package com.gongchae.gongchae_coming.member.scheduler;

import com.gongchae.gongchae_coming.member.repository.PasswordResetVerificationRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PasswordResetVerificationCleanupScheduler {

	private static final int RETENTION_HOURS = 24;

	private final PasswordResetVerificationRepository passwordResetVerificationRepository;

	@Transactional
	@Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
	public void cleanupExpiredVerifications() {
		passwordResetVerificationRepository.deleteExpiredOrUsedBefore(
			LocalDateTime.now().minusHours(RETENTION_HOURS)
		);
	}
}
