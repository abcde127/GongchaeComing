package com.gongchae.gongchae_coming.alio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.util.StringUtils;

public record AlioRecruitmentListRequest(
	String acbgCondLst,
	@Pattern(regexp = "^(R10[1-7]0)(,R10[1-7]0)*$",
		message = "hireTypeLst must contain ALIO hire type codes from R1010 to R1070, separated by comma")
	String hireTypeLst,
	String instClsf,
	@Pattern(regexp = "^(A200[1-5])(,A200[1-5])*$",
		message = "instType must contain institution type codes from A2001 to A2005, separated by comma")
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
	String searchKeyword,
	String recrutPbancTtl,
	String recrutSe,
	@Pattern(regexp = "^[YN]$", message = "replmprYn must be Y or N") String replmprYn,
	String resultType,
	@Pattern(regexp = "^(REGISTRATION_DATE|DEADLINE_DATE)$",
		message = "sortBy must be REGISTRATION_DATE or DEADLINE_DATE")
	String sortBy,
	@Pattern(regexp = "^(R30(1[0-9]|2[0-6]|30))(,R30(1[0-9]|2[0-6]|30))*$",
		message = "workRgnLst must contain ALIO work region codes from R3010 to R3026 or R3030, separated by comma")
	String workRgnLst
) {

	public int resolvedPageNo() {
		return pageNo == null ? 1 : pageNo;
	}

	public int resolvedNumOfRows() {
		return numOfRows == null ? 10 : numOfRows;
	}

	public String resolvedRecruitmentTitleKeyword() {
		if (StringUtils.hasText(recrutPbancTtl)) {
			return recrutPbancTtl.trim();
		}

		return StringUtils.hasText(searchKeyword) ? searchKeyword.trim() : null;
	}

	public String resolvedSortBy() {
		return StringUtils.hasText(sortBy) ? sortBy.trim() : "REGISTRATION_DATE";
	}
}
