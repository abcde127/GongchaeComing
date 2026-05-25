package com.gongchae.gongchae_coming.alio.repository;

import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentStatisticsRow;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AlioRecruitmentRepository extends JpaRepository<AlioRecruitment, Long>, JpaSpecificationExecutor<AlioRecruitment> {

	List<AlioRecruitment> findByRecrutPblntSnIn(Collection<Long> recruitmentSequences);

	Optional<AlioRecruitment> findByRecrutPblntSn(Long recruitmentSequence);

	@Query("select max(recruitment.recrutPblntSn) from AlioRecruitment recruitment")
	Optional<Long> findMaxRecrutPblntSn();

	@Query("select max(recruitment.createdAt) from AlioRecruitment recruitment")
	Optional<LocalDateTime> findLatestCreatedAt();

	@Query("""
		select
			recruitment.pbancBgngYmd as pbancBgngYmd,
			recruitment.pbancEndYmd as pbancEndYmd,
			recruitment.workRgnLst as workRgnLst,
			recruitment.workRgnNmLst as workRgnNmLst
		from AlioRecruitment recruitment
		""")
	List<AlioRecruitmentStatisticsRow> findStatisticsRows();

}
