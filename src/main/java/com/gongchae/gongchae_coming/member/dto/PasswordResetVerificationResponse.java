package com.gongchae.gongchae_coming.member.dto;

public record PasswordResetVerificationResponse(
	String message
) {

	public static PasswordResetVerificationResponse codeRequested() {
		return new PasswordResetVerificationResponse(
			"If the email is registered, a verification code will be sent."
		);
	}

	public static PasswordResetVerificationResponse codeVerified() {
		return new PasswordResetVerificationResponse("verification code confirmed");
	}
}
