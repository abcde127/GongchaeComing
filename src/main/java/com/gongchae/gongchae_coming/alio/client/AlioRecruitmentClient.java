package com.gongchae.gongchae_coming.alio.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.config.AlioApiProperties;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class AlioRecruitmentClient {

	private final RestClient restClient;
	private final AlioApiProperties properties;

	public JsonNode fetchRecruitments(AlioRecruitmentListRequest request) {
		validateConfiguration();

		try {
			return restClient.post()
				.uri(buildUri(request))
				.retrieve()
				.body(JsonNode.class);
		} catch (Exception exception) {
			throw new AlioApiException("Failed to fetch recruitment list from ALIO API.", exception);
		}
	}

	private URI buildUri(AlioRecruitmentListRequest request) {
		UriComponentsBuilder builder = UriComponentsBuilder
			.fromUriString(properties.baseUrl())
			.path(properties.recruitListPath())
			.queryParam("serviceKey", properties.serviceKey())
			.queryParam("resultType", resolveResultType(request))
			.queryParam("pageNo", request.resolvedPageNo())
			.queryParam("numOfRows", request.resolvedNumOfRows());

		addQueryParam(builder, "acbgCondLst", request.acbgCondLst());
		addQueryParam(builder, "hireTypeLst", request.hireTypeLst());
		addQueryParam(builder, "instClsf", request.instClsf());
		addQueryParam(builder, "instType", request.instType());
		addQueryParam(builder, "ncsCdLst", request.ncsCdLst());
		addQueryParam(builder, "ongoingYn", request.ongoingYn());
		addQueryParam(builder, "pbancBgngYmd", request.pbancBgngYmd());
		addQueryParam(builder, "pbancEndYmd", request.pbancEndYmd());
		addQueryParam(builder, "pblntInstCd", request.pblntInstCd());
		addQueryParam(builder, "recrutPbancTtl", request.recrutPbancTtl());
		addQueryParam(builder, "recrutSe", request.recrutSe());
		addQueryParam(builder, "replmprYn", request.replmprYn());
		addQueryParam(builder, "workRgnLst", request.workRgnLst());

		return builder.build(true).toUri();
	}

	private String resolveResultType(AlioRecruitmentListRequest request) {
		return StringUtils.hasText(request.resultType()) ? request.resultType() : properties.resolvedResultType();
	}

	private void addQueryParam(UriComponentsBuilder builder, String name, String value) {
		if (StringUtils.hasText(value)) {
			builder.queryParam(name, value);
		}
	}

	private void validateConfiguration() {
		if (!StringUtils.hasText(properties.baseUrl()) || !StringUtils.hasText(properties.recruitListPath())) {
			throw new AlioApiException("ALIO API baseUrl or recruitListPath is not configured.");
		}

		if (!properties.hasServiceKey()) {
			throw new AlioApiException("ALIO API serviceKey is not configured.");
		}
	}
}
