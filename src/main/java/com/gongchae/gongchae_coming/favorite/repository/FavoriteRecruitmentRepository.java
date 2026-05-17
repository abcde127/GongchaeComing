package com.gongchae.gongchae_coming.favorite.repository;

import com.gongchae.gongchae_coming.favorite.domain.FavoriteRecruitment;
import com.gongchae.gongchae_coming.favorite.domain.RecruitmentSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRecruitmentRepository extends JpaRepository<FavoriteRecruitment, Long> {

	Optional<FavoriteRecruitment> findByMemberIdAndSourceAndSourceRecruitmentId(
		Long memberId,
		RecruitmentSource source,
		String sourceRecruitmentId
	);

	List<FavoriteRecruitment> findByMemberIdOrderByCreatedAtDescIdDesc(Long memberId);

	void deleteByMemberIdAndSourceAndSourceRecruitmentId(
		Long memberId,
		RecruitmentSource source,
		String sourceRecruitmentId
	);
}
