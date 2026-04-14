package com.gongchae.gongchae_coming.member.dto;

import com.gongchae.gongchae_coming.member.domain.Member;

public record MemberResetPasswordResponse(
	Long memberId,
	String email,
	String nickname
) {

	public static MemberResetPasswordResponse from(Member member) {
		return new MemberResetPasswordResponse(
			member.getId(),
			member.getEmail(),
			member.getNickname()
		);
	}
}
