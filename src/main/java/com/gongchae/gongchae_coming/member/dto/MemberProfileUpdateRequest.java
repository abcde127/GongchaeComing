package com.gongchae.gongchae_coming.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberProfileUpdateRequest(
	@NotBlank(message = "nickname is required")
	@Size(min = 2, max = 50, message = "nickname must be between 2 and 50 characters")
	String nickname,

	@NotBlank(message = "current password is required")
	String currentPassword,

	@Size(min = 8, max = 64, message = "new password must be between 8 and 64 characters")
	@Pattern(regexp = "^$|.*[A-Za-z].*", message = "new password must include at least one letter")
	@Pattern(regexp = "^$|.*\\d.*", message = "new password must include at least one number")
	String newPassword
) {
}
