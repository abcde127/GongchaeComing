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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	private Member(String email, String nickname, String password) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
	}

	public static Member create(String email, String nickname, String encodedPassword) {
		return new Member(email, nickname, encodedPassword);
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
