package com.gongchae.gongchae_coming.member.dto;

import com.gongchae.gongchae_coming.member.domain.Member;
import java.time.LocalDateTime;

public record MemberProfileResponse(
	Long id,
	String email,
	String nickname,
	LocalDateTime createdAt
) {

	public static MemberProfileResponse from(Member member) {
		return new MemberProfileResponse(
			member.getId(),
			member.getEmail(),
			member.getNickname(),
			member.getCreatedAt()
		);
	}
}
