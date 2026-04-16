package com.gongchae.gongchae_coming.alio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record AlioRecruitmentListRequest(
	String acbgCondLst,
	String hireTypeLst,
	String instClsf,
	String instType,
	@Pattern(regexp = "^(R6000(0[1-9]|1[0-9]|2[0-5]))(,R6000(0[1-9]|1[0-9]|2[0-5]))*$",
		message = "ncsCdLst must contain ALIO NCS codes from R600001 to R600025, separated by comma")
	String ncsCdLst,
	@Min(1) @Max(100) Integer numOfRows,
	@Pattern(regexp = "^[YN]$", message = "ongoingYn must be Y or N") String ongoingYn,
	@Min(1) Integer pageNo,
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "pbancBgngYmd must be yyyy-MM-dd") String pbancBgngYmd,
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "pbancEndYmd must be yyyy-MM-dd") String pbancEndYmd,
	String pblntInstCd,
	String recrutPbancTtl,
	String recrutSe,
	@Pattern(regexp = "^[YN]$", message = "replmprYn must be Y or N") String replmprYn,
	String resultType,
	String workRgnLst
) {

	public int resolvedPageNo() {
		return pageNo == null ? 1 : pageNo;
	}

	public int resolvedNumOfRows() {
		return numOfRows == null ? 10 : numOfRows;
	}
}
