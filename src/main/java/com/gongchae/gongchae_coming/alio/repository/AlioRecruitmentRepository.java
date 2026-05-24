package com.gongchae.gongchae_coming.alio.repository;

import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlioRecruitmentRepository extends JpaRepository<AlioRecruitment, Long> {

	List<AlioRecruitment> findBySourceRecruitmentIdIn(Collection<String> sourceRecruitmentIds);

	Optional<AlioRecruitment> findBySourceRecruitmentId(String sourceRecruitmentId);
}
