package com.gongchae.gongchae_coming.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberNicknameUpdateRequest(
	@NotBlank(message = "nickname is required")
	@Size(min = 2, max = 50, message = "nickname must be between 2 and 50 characters")
	String nickname
) {
}
