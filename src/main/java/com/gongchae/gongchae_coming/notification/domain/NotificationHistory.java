package com.gongchae.gongchae_coming.notification.domain;

import com.gongchae.gongchae_coming.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_histories")
public class NotificationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private NotificationType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationSendStatus status;

	@Lob
	private String failureReason;

	@Column(nullable = false, updatable = false)
	private LocalDateTime sentAt;

	private NotificationHistory(
		Member member,
		NotificationType type,
		NotificationSendStatus status,
		String failureReason
	) {
		this.member = member;
		this.type = type;
		this.status = status;
		this.failureReason = failureReason;
	}

	public static NotificationHistory success(Member member, NotificationType type) {
		return new NotificationHistory(member, type, NotificationSendStatus.SUCCESS, null);
	}

	public static NotificationHistory failure(Member member, NotificationType type, String failureReason) {
		return new NotificationHistory(member, type, NotificationSendStatus.FAILURE, failureReason);
	}

	@PrePersist
	void prePersist() {
		sentAt = LocalDateTime.now();
	}
}
