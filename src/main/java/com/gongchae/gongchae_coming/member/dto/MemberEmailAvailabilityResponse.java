package com.gongchae.gongchae_coming.member.dto;

public record MemberEmailAvailabilityResponse(
	String email,
	boolean available
) {
}
