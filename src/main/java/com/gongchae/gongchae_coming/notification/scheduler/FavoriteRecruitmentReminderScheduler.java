package com.gongchae.gongchae_coming.notification.scheduler;

import com.gongchae.gongchae_coming.notification.service.FavoriteRecruitmentReminderService;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FavoriteRecruitmentReminderScheduler {

	private static final ZoneId REMINDER_ZONE = ZoneId.of("Asia/Seoul");

	private final FavoriteRecruitmentReminderService favoriteRecruitmentReminderService;

	@Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
	public void sendDueReminders() {
		try {
			favoriteRecruitmentReminderService.sendDueReminders(LocalTime.now(REMINDER_ZONE));
		} catch (Exception exception) {
			log.warn("Failed to send favorite recruitment reminders.", exception);
		}
	}
}
