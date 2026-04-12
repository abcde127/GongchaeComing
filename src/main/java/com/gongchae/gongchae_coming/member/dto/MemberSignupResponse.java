package com.gongchae.gongchae_coming.member.dto;

import com.gongchae.gongchae_coming.member.domain.Member;
import java.time.LocalDateTime;

public record MemberSignupResponse(
	Long id,
	String email,
	String nickname,
	LocalDateTime createdAt
) {

	public static MemberSignupResponse from(Member member) {
		return new MemberSignupResponse(
			member.getId(),
			member.getEmail(),
			member.getNickname(),
			member.getCreatedAt()
		);
	}
}
