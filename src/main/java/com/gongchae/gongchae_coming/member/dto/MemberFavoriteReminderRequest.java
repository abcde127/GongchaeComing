package com.gongchae.gongchae_coming.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberFavoriteReminderRequest(
	boolean enabled,

	@NotBlank(message = "reminderTime is required")
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "reminderTime must be HH:mm")
	String reminderTime
) {
}
