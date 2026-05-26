package com.gongchae.gongchae_coming.alio.repository;

import com.gongchae.gongchae_coming.alio.domain.AlioRecruitment;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentCategoryCountRow;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentMonthlyCountRow;
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
		where coalesce(recruitment.workRgnLst, '') like concat('%', :regionCode, '%')
		""")
	List<AlioRecruitmentStatisticsRow> findStatisticsRowsByRegionCode(@Param("regionCode") String regionCode);

	@Query("""
		select
			concat(
				substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 1, 4),
				'-',
				substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 5, 2)
			) as yearMonth,
			count(recruitment) as count
		from AlioRecruitment recruitment
		where length(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', '')) >= 6
		group by concat(
			substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 1, 4),
			'-',
			substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 5, 2)
		)
		order by yearMonth
		""")
	List<AlioRecruitmentMonthlyCountRow> findMonthlyStartCountRows();

	@Query("""
		select
			concat(
				substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 1, 4),
				'-',
				substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 5, 2)
			) as yearMonth,
			count(recruitment) as count
		from AlioRecruitment recruitment
		where length(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', '')) >= 6
			and coalesce(recruitment.workRgnLst, '') like concat('%', :regionCode, '%')
		group by concat(
			substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 1, 4),
			'-',
			substring(function('replace', coalesce(recruitment.pbancBgngYmd, ''), '-', ''), 5, 2)
		)
		order by yearMonth
		""")
	List<AlioRecruitmentMonthlyCountRow> findMonthlyStartCountRowsByRegionCode(@Param("regionCode") String regionCode);

	@Query("""
		select
			recruitment.pblntInstCd as code,
			recruitment.instNm as label,
			count(recruitment) as count
		from AlioRecruitment recruitment
		where coalesce(recruitment.pblntInstCd, recruitment.instNm) is not null
		group by recruitment.pblntInstCd, recruitment.instNm
		order by count(recruitment) desc, recruitment.instNm asc
		""")
	List<AlioRecruitmentCategoryCountRow> findCompanyCountRows();

	@Query("""
		select
			recruitment.pblntInstCd as code,
			recruitment.instNm as label,
			count(recruitment) as count
		from AlioRecruitment recruitment
		where coalesce(recruitment.pblntInstCd, recruitment.instNm) is not null
			and coalesce(recruitment.workRgnLst, '') like concat('%', :regionCode, '%')
		group by recruitment.pblntInstCd, recruitment.instNm
		order by count(recruitment) desc, recruitment.instNm asc
		""")
	List<AlioRecruitmentCategoryCountRow> findCompanyCountRowsByRegionCode(@Param("regionCode") String regionCode);

}
