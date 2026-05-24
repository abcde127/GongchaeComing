package com.gongchae.gongchae_coming.notification.dto;

import com.gongchae.gongchae_coming.notification.domain.NotificationHistory;
import java.time.LocalDateTime;

public record NotificationHistoryResponse(
	Long id,
	String type,
	String typeLabel,
	LocalDateTime sentAt,
	String status,
	String statusLabel,
	String failureReason
) {

	public static NotificationHistoryResponse from(NotificationHistory history) {
		return new NotificationHistoryResponse(
			history.getId(),
			history.getType().name(),
			history.getType().getDisplayName(),
			history.getSentAt(),
			history.getStatus().name(),
			history.getStatus().getDisplayName(),
			history.getFailureReason()
		);
	}
}
