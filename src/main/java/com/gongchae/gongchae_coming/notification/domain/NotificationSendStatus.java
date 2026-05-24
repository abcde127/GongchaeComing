package com.gongchae.gongchae_coming.notification.domain;

public enum NotificationSendStatus {
	SUCCESS("성공"),
	FAILURE("실패");

	private final String displayName;

	NotificationSendStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
