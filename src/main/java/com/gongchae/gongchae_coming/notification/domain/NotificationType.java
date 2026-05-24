package com.gongchae.gongchae_coming.notification.domain;

public enum NotificationType {
	FAVORITE_RECRUITMENT_REMINDER("관심공고 리마인드"),
	NEW_RECRUITMENT("신규공고 알림");

	private final String displayName;

	NotificationType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
