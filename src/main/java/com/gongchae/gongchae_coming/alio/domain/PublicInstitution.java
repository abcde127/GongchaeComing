package com.gongchae.gongchae_coming.alio.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "public_institutions")
public class PublicInstitution {

	@Id
	@Column(length = 100)
	private String instCd;

	@Column(length = 100)
	private String pbadmsStdInstCd;

	@Column(nullable = false, length = 255)
	private String instNm;

	@Column(length = 10)
	private String laygInstYn;

	@Column(length = 100)
	private String sprvsnInstCd;

	@Column(length = 255)
	private String sprvsnInstNm;

	@Column(length = 100)
	private String instType;

	@Column(length = 100)
	private String instTypeNm;

	@Column(length = 100)
	private String instClsf;

	@Column(length = 100)
	private String instClsfNm;

	@Column(length = 10)
	private String dsgnYn;

	@Column(length = 10)
	private String frgnYn;

	@Column(nullable = false)
	private LocalDateTime fetchedAt;

	protected PublicInstitution() {
	}

	private PublicInstitution(String instCd) {
		this.instCd = instCd;
	}

	public static PublicInstitution from(JsonNode item, LocalDateTime fetchedAt) {
		PublicInstitution institution = new PublicInstitution(text(item, "instCd"));
		institution.updateFrom(item, fetchedAt);
		return institution;
	}

	public void updateFrom(JsonNode item, LocalDateTime fetchedAt) {
		this.pbadmsStdInstCd = text(item, "pbadmsStdInstCd");
		this.instNm = text(item, "instNm");
		this.laygInstYn = text(item, "laygInstYn");
		this.sprvsnInstCd = text(item, "sprvsnInstCd");
		this.sprvsnInstNm = text(item, "sprvsnInstNm");
		this.instType = text(item, "instType");
		this.instTypeNm = text(item, "instTypeNm");
		this.instClsf = text(item, "instClsf");
		this.instClsfNm = text(item, "instClsfNm");
		this.dsgnYn = text(item, "dsgnYn");
		this.frgnYn = text(item, "frgnYn");
		this.fetchedAt = fetchedAt;
	}

	public String getInstCd() {
		return instCd;
	}

	public String getInstNm() {
		return instNm;
	}

	private static String text(JsonNode item, String fieldName) {
		JsonNode value = item.path(fieldName);
		if (value.isMissingNode() || value.isNull()) {
			return null;
		}
		String text = value.asText();
		return text.isBlank() ? null : text;
	}
}
