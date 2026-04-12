package com.gongchae.gongchae_coming.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberSignupRequest(
	@NotBlank(message = "email is required")
	@Email(message = "email must be valid")
	@Size(max = 100, message = "email must be 100 characters or less")
	String email,

	@NotBlank(message = "nickname is required")
	@Size(min = 2, max = 50, message = "nickname must be between 2 and 50 characters")
	String nickname,

	@NotBlank(message = "password is required")
	@Size(min = 8, max = 64, message = "password must be between 8 and 64 characters")
	@Pattern(regexp = ".*[A-Za-z].*", message = "password must include at least one letter")
	@Pattern(regexp = ".*\\d.*", message = "password must include at least one number")
	String password
) {
}
