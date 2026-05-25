package com.gongchae.gongchae_coming.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetCodeVerifyRequest(
	@NotBlank(message = "email is required")
	@Email(message = "email must be valid")
	@Size(max = 100, message = "email must be 100 characters or less")
	String email,

	@NotBlank(message = "verification code is required")
	@Pattern(regexp = "\\d{6}", message = "verification code must be 6 digits")
	String code
) {
}
