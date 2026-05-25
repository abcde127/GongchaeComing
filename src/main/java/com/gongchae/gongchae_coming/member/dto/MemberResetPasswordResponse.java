package com.gongchae.gongchae_coming.member.dto;

public record MemberResetPasswordResponse(
	String message
) {

	public static MemberResetPasswordResponse completed() {
		return new MemberResetPasswordResponse("password reset completed");
	}
}
