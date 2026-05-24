package com.gongchae.gongchae_coming.alio.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "alio_recruitments")
public class AlioRecruitment {

	@Id
	private Long recrutPblntSn;

	@Column(length = 100)
	private String recrutPbancSn;

	@Column(length = 100)
	private String pblntInstCd;

	@Column(length = 255)
	private String pblntInstNm;

	@Column(length = 100)
	private String instNm;

	@Column(length = 100)
	private String instClsf;

	@Column(length = 100)
	private String instClsfNm;

	@Column(length = 100)
	private String instType;

	@Column(length = 100)
	private String instTypeNm;

	@Column(length = 500)
	private String recrutPbancTtl;

	@Column(length = 100)
	private String recrutSe;

	@Column(length = 100)
	private String recrutSeNm;

	@Lob
	private String hireTypeLst;

	@Lob
	private String hireTypeNmLst;

	@Lob
	private String workRgnLst;

	@Lob
	private String workRgnNmLst;

	@Lob
	private String ncsCdLst;

	@Lob
	private String ncsCdNmLst;

	@Lob
	private String acbgCondLst;

	@Lob
	private String acbgCondNmLst;

	@Column(length = 10)
	private String replmprYn;

	@Column(length = 30)
	private String pbancBgngYmd;

	@Column(length = 30)
	private String pbancEndYmd;

	@Column(length = 30)
	private String pbancRgtrYmd;

	@Column(length = 30)
	private String aplyEndYmd;

	@Column(length = 500)
	private String recrutPbancUrl;

	@Column(length = 500)
	private String srcUrl;

	@Column(nullable = false)
	private LocalDateTime fetchedAt;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected AlioRecruitment() {
	}

	public static AlioRecruitment from(JsonNode item, LocalDateTime fetchedAt) {
		AlioRecruitment recruitment = new AlioRecruitment();
		recruitment.updateFrom(item, fetchedAt);
		return recruitment;
	}

	public void updateFrom(JsonNode item, LocalDateTime fetchedAt) {
		this.recrutPblntSn = resolveRecruitmentSequence(item);
		this.recrutPbancSn = text(item, "recrutPbancSn");
		this.pblntInstCd = text(item, "pblntInstCd");
		this.pblntInstNm = text(item, "pblntInstNm");
		this.instNm = text(item, "instNm");
		this.instClsf = text(item, "instClsf");
		this.instClsfNm = text(item, "instClsfNm");
		this.instType = text(item, "instType");
		this.instTypeNm = text(item, "instTypeNm");
		this.recrutPbancTtl = text(item, "recrutPbancTtl");
		this.recrutSe = text(item, "recrutSe");
		this.recrutSeNm = text(item, "recrutSeNm");
		this.hireTypeLst = text(item, "hireTypeLst", "hireTypeCdLst", "hireTypeCd");
		this.hireTypeNmLst = text(item, "hireTypeNmLst", "hireTypeNm", "hireType");
		this.workRgnLst = text(item, "workRgnLst", "workRgnCdLst", "workRgnCd", "workRegionCode");
		this.workRgnNmLst = text(item, "workRgnNmLst", "workRgnNm", "workRegionNm");
		this.ncsCdLst = text(item, "ncsCdLst", "ncsCd", "ncsCode");
		this.ncsCdNmLst = text(item, "ncsCdNmLst", "ncsNmLst", "ncsNm", "ncsName");
		this.acbgCondLst = text(item, "acbgCondLst");
		this.acbgCondNmLst = text(item, "acbgCondNmLst");
		this.replmprYn = text(item, "replmprYn");
		this.pbancBgngYmd = text(item, "pbancBgngYmd");
		this.pbancEndYmd = text(item, "pbancEndYmd");
		this.pbancRgtrYmd = text(item, "pbancRgtrYmd", "regDt", "frstRegDt", "registrationDate");
		this.aplyEndYmd = text(item, "aplyEndYmd", "endDate");
		this.recrutPbancUrl = text(item, "recrutPbancUrl", "url");
		this.srcUrl = text(item, "srcUrl");
		this.fetchedAt = fetchedAt;
	}

	public void writeTo(ObjectNode node) {
		put(node, "recrutPblntSn", recrutPblntSn);
		put(node, "recrutPbancSn", recrutPbancSn);
		put(node, "pblntInstCd", pblntInstCd);
		put(node, "pblntInstNm", pblntInstNm);
		put(node, "instNm", instNm);
		put(node, "instClsf", instClsf);
		put(node, "instClsfNm", instClsfNm);
		put(node, "instType", instType);
		put(node, "instTypeNm", instTypeNm);
		put(node, "recrutPbancTtl", recrutPbancTtl);
		put(node, "recrutSe", recrutSe);
		put(node, "recrutSeNm", recrutSeNm);
		put(node, "hireTypeLst", hireTypeLst);
		put(node, "hireTypeNmLst", hireTypeNmLst);
		put(node, "workRgnLst", workRgnLst);
		put(node, "workRgnNmLst", workRgnNmLst);
		put(node, "ncsCdLst", ncsCdLst);
		put(node, "ncsCdNmLst", ncsCdNmLst);
		put(node, "acbgCondLst", acbgCondLst);
		put(node, "acbgCondNmLst", acbgCondNmLst);
		put(node, "replmprYn", replmprYn);
		put(node, "pbancBgngYmd", pbancBgngYmd);
		put(node, "pbancEndYmd", pbancEndYmd);
		put(node, "pbancRgtrYmd", pbancRgtrYmd);
		put(node, "aplyEndYmd", aplyEndYmd);
		put(node, "recrutPbancUrl", recrutPbancUrl);
		put(node, "srcUrl", srcUrl);
	}

	public String getSourceRecruitmentId() {
		return recrutPblntSn == null ? null : String.valueOf(recrutPblntSn);
	}

	public Long getRecrutPblntSn() {
		return recrutPblntSn;
	}

	public String getRecruitmentUrl() {
		if (StringUtils.hasText(recrutPbancUrl)) {
			return recrutPbancUrl;
		}
		return srcUrl;
	}

	public LocalDateTime getFetchedAt() {
		return fetchedAt;
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public static Long resolveRecruitmentSequence(JsonNode item) {
		return longValue(item, "recrutPblntSn");
	}

	private static String text(JsonNode item, String... fieldNames) {
		for (String fieldName : fieldNames) {
			String value = item.path(fieldName).asText(null);
			if (StringUtils.hasText(value)) {
				return value.trim();
			}
		}
		return null;
	}

	private static void put(ObjectNode node, String fieldName, String value) {
		if (value != null) {
			node.put(fieldName, value);
		}
	}

	private static void put(ObjectNode node, String fieldName, Long value) {
		if (value != null) {
			node.put(fieldName, value);
		}
	}

	private static void put(ObjectNode node, String fieldName, Integer value) {
		if (value != null) {
			node.put(fieldName, value);
		}
	}

	private static Long longValue(JsonNode item, String fieldName) {
		JsonNode value = item.path(fieldName);
		return value.isNumber() || StringUtils.hasText(value.asText(null)) ? value.asLong() : null;
	}

}
