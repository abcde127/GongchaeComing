package com.gongchae.gongchae_coming.member.dto;

import com.gongchae.gongchae_coming.member.domain.Member;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MemberProfileResponse(
	Long id,
	String email,
	String nickname,
	LocalDateTime createdAt,
	boolean kakaoLinked,
	LocalDateTime kakaoLinkedAt,
	boolean favoriteReminderEnabled,
	LocalTime favoriteReminderTime
) {

	public static MemberProfileResponse from(Member member) {
		return new MemberProfileResponse(
			member.getId(),
			member.getEmail(),
			member.getNickname(),
			member.getCreatedAt(),
			member.isKakaoLinked(),
			member.getKakaoLinkedAt(),
			member.isFavoriteReminderEnabled(),
			member.getFavoriteReminderTime()
		);
	}
}
