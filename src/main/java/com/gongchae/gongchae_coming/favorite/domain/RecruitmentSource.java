package com.gongchae.gongchae_coming.favorite.domain;

import java.util.Arrays;

public enum RecruitmentSource {

	ALIO;

	public static RecruitmentSource from(String value) {
		if (value == null || value.isBlank()) {
			return ALIO;
		}

		return Arrays.stream(values())
			.filter(source -> source.name().equalsIgnoreCase(value.trim()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("unsupported recruitment source"));
	}
}
