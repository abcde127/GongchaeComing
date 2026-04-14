package com.gongchae.gongchae_coming.member.dto;

import com.gongchae.gongchae_coming.member.domain.Member;

public record MemberFindIdResponse(
	Long memberId,
	String email,
	String nickname
) {

	public static MemberFindIdResponse from(Member member) {
		return new MemberFindIdResponse(
			member.getId(),
			member.getEmail(),
			member.getNickname()
		);
	}
}
