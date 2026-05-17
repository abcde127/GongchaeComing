package com.gongchae.gongchae_coming.favorite.service;

import com.gongchae.gongchae_coming.favorite.domain.FavoriteRecruitment;
import com.gongchae.gongchae_coming.favorite.domain.RecruitmentSource;
import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentCreateRequest;
import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentResponse;
import com.gongchae.gongchae_coming.favorite.repository.FavoriteRecruitmentRepository;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteRecruitmentService {

	private final FavoriteRecruitmentRepository favoriteRecruitmentRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public FavoriteRecruitmentResponse create(Long memberId, FavoriteRecruitmentCreateRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberNotFoundException("member not found"));

		RecruitmentSource source = RecruitmentSource.from(request.source());

		return favoriteRecruitmentRepository
			.findByMemberIdAndSourceAndSourceRecruitmentId(memberId, source, request.sourceRecruitmentId())
			.map(favoriteRecruitment -> FavoriteRecruitmentResponse.from(favoriteRecruitment, false))
			.orElseGet(() -> FavoriteRecruitmentResponse.from(
				favoriteRecruitmentRepository.save(
					FavoriteRecruitment.create(
						member,
						source,
						request.sourceRecruitmentId(),
						request.recruitmentTitle(),
						request.institutionName(),
						request.hireType(),
						request.workRegion(),
						request.recruitmentStartDate(),
						request.recruitmentEndDate(),
						request.recruitmentUrl()
					)
				),
				true
			));
	}

	@Transactional(readOnly = true)
	public List<FavoriteRecruitmentResponse> getFavorites(Long memberId) {
		validateMemberExists(memberId);

		return favoriteRecruitmentRepository.findByMemberIdOrderByCreatedAtDescIdDesc(memberId).stream()
			.map(favoriteRecruitment -> FavoriteRecruitmentResponse.from(favoriteRecruitment, false))
			.toList();
	}

	@Transactional
	public void delete(Long memberId, String source, String sourceRecruitmentId) {
		validateMemberExists(memberId);

		favoriteRecruitmentRepository.deleteByMemberIdAndSourceAndSourceRecruitmentId(
			memberId,
			RecruitmentSource.from(source),
			sourceRecruitmentId
		);
	}

	private void validateMemberExists(Long memberId) {
		if (!memberRepository.existsById(memberId)) {
			throw new MemberNotFoundException("member not found");
		}
	}
}
