package com.gongchae.gongchae_coming.favorite.domain;

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
import jakarta.persistence.ManyToOne;
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
	name = "favorite_recruitments",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_favorite_recruitment_member_source_recruitment",
			columnNames = {"member_id", "source", "source_recruitment_id"}
		)
	}
)
public class FavoriteRecruitment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RecruitmentSource source;

	@Column(name = "source_recruitment_id", nullable = false, length = 100)
	private String sourceRecruitmentId;

	@Column(nullable = false, length = 255)
	private String recruitmentTitle;

	@Column(nullable = false, length = 100)
	private String institutionName;

	@Column(length = 100)
	private String hireType;

	@Column(length = 100)
	private String workRegion;

	@Column(length = 30)
	private String recruitmentStartDate;

	@Column(length = 30)
	private String recruitmentEndDate;

	@Column(length = 500)
	private String recruitmentUrl;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private FavoriteRecruitment(
		Member member,
		RecruitmentSource source,
		String sourceRecruitmentId,
		String recruitmentTitle,
		String institutionName,
		String hireType,
		String workRegion,
		String recruitmentStartDate,
		String recruitmentEndDate,
		String recruitmentUrl
	) {
		this.member = member;
		this.source = source;
		this.sourceRecruitmentId = sourceRecruitmentId;
		this.recruitmentTitle = recruitmentTitle;
		this.institutionName = institutionName;
		this.hireType = hireType;
		this.workRegion = workRegion;
		this.recruitmentStartDate = recruitmentStartDate;
		this.recruitmentEndDate = recruitmentEndDate;
		this.recruitmentUrl = recruitmentUrl;
	}

	public static FavoriteRecruitment create(
		Member member,
		RecruitmentSource source,
		String sourceRecruitmentId,
		String recruitmentTitle,
		String institutionName,
		String hireType,
		String workRegion,
		String recruitmentStartDate,
		String recruitmentEndDate,
		String recruitmentUrl
	) {
		return new FavoriteRecruitment(
			member,
			source,
			sourceRecruitmentId,
			recruitmentTitle,
			institutionName,
			hireType,
			workRegion,
			recruitmentStartDate,
			recruitmentEndDate,
			recruitmentUrl
		);
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
