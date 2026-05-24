package com.gongchae.gongchae_coming.alio.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.springframework.util.StringUtils;

@Entity
@Table(
	name = "alio_recruitments",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_alio_recruitment_source_id", columnNames = "source_recruitment_id")
	}
)
public class AlioRecruitment {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "source_recruitment_id", nullable = false, length = 100)
	private String sourceRecruitmentId;

	private Long recrutPblntSn;

	@Column(length = 100)
	private String recrutPbancSn;

	@Column(length = 100)
	private String pblntInstCd;

	@Column(length = 100)
	private String pbadmsStdInstCd;

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
	private String prefCondCn;

	private Integer recrutNope;

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

	@Column(length = 100)
	private String replmprYnNm;

	@Column(length = 10)
	private String ongoingYn;

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

	@Lob
	private String aplyQlfcCn;

	@Lob
	private String disqlfcRsn;

	@Lob
	private String scrnprcdrMthdExpln;

	@Lob
	private String prefCn;

	@Lob
	private String nonatchRsn;

	private Integer decimalDay;

	@Lob
	private String files;

	@Lob
	private String steps;

	@Column(nullable = false)
	private LocalDateTime fetchedAt;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected AlioRecruitment() {
	}

	private AlioRecruitment(String sourceRecruitmentId) {
		this.sourceRecruitmentId = sourceRecruitmentId;
	}

	public static AlioRecruitment from(JsonNode item, LocalDateTime fetchedAt) {
		AlioRecruitment recruitment = new AlioRecruitment(resolveSourceRecruitmentId(item));
		recruitment.updateFrom(item, fetchedAt);
		return recruitment;
	}

	public void updateFrom(JsonNode item, LocalDateTime fetchedAt) {
		this.recrutPblntSn = longValue(item, "recrutPblntSn");
		this.recrutPbancSn = text(item, "recrutPbancSn");
		this.pblntInstCd = text(item, "pblntInstCd");
		this.pbadmsStdInstCd = text(item, "pbadmsStdInstCd");
		this.pblntInstNm = text(item, "pblntInstNm");
		this.instNm = text(item, "instNm");
		this.instClsf = text(item, "instClsf");
		this.instClsfNm = text(item, "instClsfNm");
		this.instType = text(item, "instType");
		this.instTypeNm = text(item, "instTypeNm");
		this.recrutPbancTtl = text(item, "recrutPbancTtl");
		this.recrutSe = text(item, "recrutSe");
		this.recrutSeNm = text(item, "recrutSeNm");
		this.prefCondCn = text(item, "prefCondCn");
		this.recrutNope = integerValue(item, "recrutNope");
		this.hireTypeLst = text(item, "hireTypeLst", "hireTypeCdLst", "hireTypeCd");
		this.hireTypeNmLst = text(item, "hireTypeNmLst", "hireTypeNm", "hireType");
		this.workRgnLst = text(item, "workRgnLst", "workRgnCdLst", "workRgnCd", "workRegionCode");
		this.workRgnNmLst = text(item, "workRgnNmLst", "workRgnNm", "workRegionNm");
		this.ncsCdLst = text(item, "ncsCdLst", "ncsCd", "ncsCode");
		this.ncsCdNmLst = text(item, "ncsCdNmLst", "ncsNmLst", "ncsNm", "ncsName");
		this.acbgCondLst = text(item, "acbgCondLst");
		this.acbgCondNmLst = text(item, "acbgCondNmLst");
		this.replmprYn = text(item, "replmprYn");
		this.replmprYnNm = text(item, "replmprYnNm");
		this.ongoingYn = text(item, "ongoingYn");
		this.pbancBgngYmd = text(item, "pbancBgngYmd");
		this.pbancEndYmd = text(item, "pbancEndYmd");
		this.pbancRgtrYmd = text(item, "pbancRgtrYmd", "regDt", "frstRegDt", "registrationDate");
		this.aplyEndYmd = text(item, "aplyEndYmd", "endDate");
		this.recrutPbancUrl = text(item, "recrutPbancUrl", "url");
		this.srcUrl = text(item, "srcUrl");
		this.aplyQlfcCn = text(item, "aplyQlfcCn");
		this.disqlfcRsn = text(item, "disqlfcRsn");
		this.scrnprcdrMthdExpln = text(item, "scrnprcdrMthdExpln");
		this.prefCn = text(item, "prefCn");
		this.nonatchRsn = text(item, "nonatchRsn");
		this.decimalDay = integerValue(item, "decimalDay");
		this.files = jsonText(item, "files");
		this.steps = jsonText(item, "steps");
		this.fetchedAt = fetchedAt;
	}

	public void writeTo(ObjectNode node) {
		put(node, "recrutPblntSn", recrutPblntSn);
		put(node, "recrutPbancSn", recrutPbancSn);
		put(node, "pblntInstCd", pblntInstCd);
		put(node, "pbadmsStdInstCd", pbadmsStdInstCd);
		put(node, "pblntInstNm", pblntInstNm);
		put(node, "instNm", instNm);
		put(node, "instClsf", instClsf);
		put(node, "instClsfNm", instClsfNm);
		put(node, "instType", instType);
		put(node, "instTypeNm", instTypeNm);
		put(node, "recrutPbancTtl", recrutPbancTtl);
		put(node, "recrutSe", recrutSe);
		put(node, "recrutSeNm", recrutSeNm);
		put(node, "prefCondCn", prefCondCn);
		put(node, "recrutNope", recrutNope);
		put(node, "hireTypeLst", hireTypeLst);
		put(node, "hireTypeNmLst", hireTypeNmLst);
		put(node, "workRgnLst", workRgnLst);
		put(node, "workRgnNmLst", workRgnNmLst);
		put(node, "ncsCdLst", ncsCdLst);
		put(node, "ncsCdNmLst", ncsCdNmLst);
		put(node, "acbgCondLst", acbgCondLst);
		put(node, "acbgCondNmLst", acbgCondNmLst);
		put(node, "replmprYn", replmprYn);
		put(node, "replmprYnNm", replmprYnNm);
		put(node, "ongoingYn", ongoingYn);
		put(node, "pbancBgngYmd", pbancBgngYmd);
		put(node, "pbancEndYmd", pbancEndYmd);
		put(node, "pbancRgtrYmd", pbancRgtrYmd);
		put(node, "aplyEndYmd", aplyEndYmd);
		put(node, "recrutPbancUrl", recrutPbancUrl);
		put(node, "srcUrl", srcUrl);
		put(node, "aplyQlfcCn", aplyQlfcCn);
		put(node, "disqlfcRsn", disqlfcRsn);
		put(node, "scrnprcdrMthdExpln", scrnprcdrMthdExpln);
		put(node, "prefCn", prefCn);
		put(node, "nonatchRsn", nonatchRsn);
		put(node, "decimalDay", decimalDay);
		putJson(node, "files", files);
		putJson(node, "steps", steps);
	}

	public String getSourceRecruitmentId() {
		return sourceRecruitmentId;
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

	public static String resolveSourceRecruitmentId(JsonNode item) {
		String sourceId = text(item, "recrutPblntSn", "recrutPbancSn", "pbancSn");
		if (StringUtils.hasText(sourceId)) {
			return sourceId;
		}

		String fallback = String.join("|",
			text(item, "pblntInstCd", "pblntInstNm", "instNm"),
			text(item, "recrutPbancTtl"),
			text(item, "pbancBgngYmd", "pbancRgtrYmd"),
			text(item, "pbancEndYmd", "aplyEndYmd")
		);
		return "generated-" + sha256(fallback);
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

	private static void putJson(ObjectNode node, String fieldName, String value) {
		if (value == null) {
			return;
		}

		try {
			node.set(fieldName, OBJECT_MAPPER.readTree(value));
		} catch (Exception ignored) {
			node.put(fieldName, value);
		}
	}

	private static Long longValue(JsonNode item, String fieldName) {
		JsonNode value = item.path(fieldName);
		return value.isNumber() || StringUtils.hasText(value.asText(null)) ? value.asLong() : null;
	}

	private static Integer integerValue(JsonNode item, String fieldName) {
		JsonNode value = item.path(fieldName);
		return value.isNumber() || StringUtils.hasText(value.asText(null)) ? value.asInt() : null;
	}

	private static String jsonText(JsonNode item, String fieldName) {
		JsonNode value = item.path(fieldName);
		if (value.isMissingNode() || value.isNull()) {
			return null;
		}
		return value.toString();
	}

	private static String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed).substring(0, 32);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 digest is not available.", exception);
		}
	}
}
