package com.gongchae.gongchae_coming.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "password_reset_verification",
	indexes = {
		@Index(name = "idx_password_reset_verification_email_created_at", columnList = "email, created_at"),
		@Index(name = "idx_password_reset_verification_expires_at", columnList = "expires_at")
	}
)
public class PasswordResetVerification {

	public static final int MAX_ATTEMPT_COUNT = 5;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String email;

	@Column(name = "code_hash", nullable = false, length = 100)
	private String codeHash;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	private PasswordResetVerification(String email, String codeHash, LocalDateTime expiresAt) {
		this.email = email;
		this.codeHash = codeHash;
		this.expiresAt = expiresAt;
	}

	public static PasswordResetVerification create(String email, String codeHash, LocalDateTime expiresAt) {
		return new PasswordResetVerification(email, codeHash, expiresAt);
	}

	public boolean canVerify(LocalDateTime now) {
		return usedAt == null
			&& verifiedAt == null
			&& !expiresAt.isBefore(now)
			&& attemptCount < MAX_ATTEMPT_COUNT;
	}

	public boolean isVerifiedAndUsable(LocalDateTime now) {
		return usedAt == null
			&& verifiedAt != null
			&& !expiresAt.isBefore(now)
			&& attemptCount <= MAX_ATTEMPT_COUNT;
	}

	public void increaseAttemptCount() {
		attemptCount++;
	}

	public void verify(LocalDateTime now) {
		this.verifiedAt = now;
	}

	public void use(LocalDateTime now) {
		this.usedAt = now;
	}

	public void invalidate(LocalDateTime now) {
		if (usedAt == null) {
			this.usedAt = now;
		}
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
