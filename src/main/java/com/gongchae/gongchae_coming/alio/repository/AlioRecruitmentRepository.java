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
import org.springframework.data.repository.query.Param;

public interface AlioRecruitmentRepository extends JpaRepository<AlioRecruitment, Long>, JpaSpecificationExecutor<AlioRecruitment> {

	List<AlioRecruitment> findByRecrutPblntSnIn(Collection<Long> recruitmentSequences);

	Optional<AlioRecruitment> findByRecrutPblntSn(Long recruitmentSequence);

	@Query("select max(recruitment.recrutPblntSn) from AlioRecruitment recruitment")
	Optional<Long> findMaxRecrutPblntSn();

	@Query("select max(recruitment.createdAt) from AlioRecruitment recruitment")
	Optional<LocalDateTime> findLatestCreatedAt();

	@Query("""
		select count(recruitment)
		from AlioRecruitment recruitment
		where function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', '') > :today
		""")
	long countScheduledRecruitments(@Param("today") String today);

	@Query("""
		select count(recruitment)
		from AlioRecruitment recruitment
		where function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', '') <= :today
			and function('replace', coalesce(recruitment.pbancEndYmd, ''), '-', '') >= :today
		""")
	long countActiveRecruitments(@Param("today") String today);

	@Query("""
		select
			recruitment.pbancBgngYmd as pbancBgngYmd,
			recruitment.pbancEndYmd as pbancEndYmd,
			recruitment.workRgnLst as workRgnLst,
			recruitment.workRgnNmLst as workRgnNmLst,
			recruitment.pblntInstCd as pblntInstCd,
			recruitment.instNm as instNm,
			recruitment.ncsCdLst as ncsCdLst,
			recruitment.ncsCdNmLst as ncsCdNmLst,
			recruitment.recrutSe as recrutSe,
			recruitment.recrutSeNm as recrutSeNm,
			recruitment.hireTypeLst as hireTypeLst,
			recruitment.hireTypeNmLst as hireTypeNmLst
		from AlioRecruitment recruitment
		""")
	List<AlioRecruitmentStatisticsRow> findStatisticsRows();

}
