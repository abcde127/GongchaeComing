package com.gongchae.gongchae_coming.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberPasswordVerificationRequest(
	@NotBlank(message = "current password is required")
	String currentPassword
) {
}
