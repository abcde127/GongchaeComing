package com.gongchae.gongchae_coming.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "members",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_members_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_members_nickname", columnNames = "nickname")
	}
)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String email;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Column(nullable = false, length = 100)
	private String password;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(length = 100)
	private String preferredSearchKeyword;

	@Column(length = 3000)
	private String preferredCompanies;

	@Column(length = 80)
	private String preferredRecruitmentStatuses;

	@Column(length = 500)
	private String preferredRegions;

	@Column(length = 80)
	private String preferredCategories;

	@Column(length = 120)
	private String preferredHireTypes;

	@Column(length = 500)
	private String preferredNcsCodes;

	@Column(length = 2000)
	private String kakaoAccessToken;

	private Integer kakaoAccessTokenExpiresIn;

	@Column(length = 2000)
	private String kakaoRefreshToken;

	private Integer kakaoRefreshTokenExpiresIn;

	private LocalDateTime kakaoLinkedAt;

	private Boolean favoriteReminderEnabled = false;

	private LocalTime favoriteReminderTime;

	private Member(String email, String nickname, String password) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
	}

	public static Member create(String email, String nickname, String encodedPassword) {
		return new Member(email, nickname, encodedPassword);
	}

	public void resetPassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateJobPreference(
		String searchKeyword,
		String companies,
		String recruitmentStatuses,
		String regions,
		String categories,
		String hireTypes,
		String ncsCodes
	) {
		this.preferredSearchKeyword = searchKeyword;
		this.preferredCompanies = companies;
		this.preferredRecruitmentStatuses = recruitmentStatuses;
		this.preferredRegions = regions;
		this.preferredCategories = categories;
		this.preferredHireTypes = hireTypes;
		this.preferredNcsCodes = ncsCodes;
	}

	public void updateKakaoToken(
		String accessToken,
		Integer accessTokenExpiresIn,
		String refreshToken,
		Integer refreshTokenExpiresIn
	) {
		this.kakaoAccessToken = accessToken;
		this.kakaoAccessTokenExpiresIn = accessTokenExpiresIn;
		this.kakaoRefreshToken = refreshToken;
		this.kakaoRefreshTokenExpiresIn = refreshTokenExpiresIn;
		this.kakaoLinkedAt = LocalDateTime.now();
	}

	public void updateKakaoAccessToken(String accessToken, Integer accessTokenExpiresIn) {
		this.kakaoAccessToken = accessToken;
		this.kakaoAccessTokenExpiresIn = accessTokenExpiresIn;
	}

	public void updateKakaoRefreshToken(String refreshToken, Integer refreshTokenExpiresIn) {
		this.kakaoRefreshToken = refreshToken;
		this.kakaoRefreshTokenExpiresIn = refreshTokenExpiresIn;
	}

	public boolean isKakaoLinked() {
		return StringUtils.hasText(kakaoAccessToken)
			&& kakaoAccessTokenExpiresIn != null
			&& StringUtils.hasText(kakaoRefreshToken)
			&& kakaoRefreshTokenExpiresIn != null;
	}

	public boolean isFavoriteReminderEnabled() {
		return Boolean.TRUE.equals(favoriteReminderEnabled);
	}

	public void updateFavoriteReminder(boolean enabled, LocalTime reminderTime) {
		this.favoriteReminderEnabled = enabled;
		this.favoriteReminderTime = reminderTime;
	}

	public List<String> splitPreferredCompanies() {
		return splitCsv(preferredCompanies);
	}

	public List<String> splitPreferredRecruitmentStatuses() {
		return splitCsv(preferredRecruitmentStatuses);
	}

	public List<String> splitPreferredRegions() {
		return splitCsv(preferredRegions);
	}

	public List<String> splitPreferredCategories() {
		return splitCsv(preferredCategories);
	}

	public List<String> splitPreferredHireTypes() {
		return splitCsv(preferredHireTypes);
	}

	public List<String> splitPreferredNcsCodes() {
		return splitCsv(preferredNcsCodes);
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}

	private List<String> splitCsv(String value) {
		if (!StringUtils.hasText(value)) {
			return List.of();
		}
		return Arrays.stream(value.split(","))
			.map(String::trim)
			.filter(StringUtils::hasText)
			.toList();
	}
}
