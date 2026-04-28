package com.gongchae.gongchae_coming.alio.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.gongchae.gongchae_coming.alio.config.AlioApiProperties;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class AlioRecruitmentClient {

	private final RestClient restClient;
	private final AlioApiProperties properties;

	public JsonNode fetchRecruitments(AlioRecruitmentListRequest request) {
		validateConfiguration();
		URI uri = buildUri(request);

		try {
			return restClient.post()
				.uri(uri)
				.retrieve()
				.body(JsonNode.class);
		} catch (Exception exception) {
			throw buildAlioApiException(uri, exception);
		}
	}

	public String buildRequestUriForDebug(AlioRecruitmentListRequest request) {
		validateConfiguration();
		return buildUri(request).toString();
	}

	private URI buildUri(AlioRecruitmentListRequest request) {
		String recruitmentTitleKeyword = request.resolvedRecruitmentTitleKeyword();
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
		addQueryParam(builder, "recrutSe", request.recrutSe());
		addQueryParam(builder, "replmprYn", request.replmprYn());
		addQueryParam(builder, "workRgnLst", request.workRgnLst());

		String uri = builder.build().toUriString();
		if (StringUtils.hasText(recruitmentTitleKeyword)) {
			uri = uri + "&recrutPbancTl=" + toRawTitleKeywordQueryValue(recruitmentTitleKeyword);
		}

		return URI.create(uri);
	}

	private String resolveResultType(AlioRecruitmentListRequest request) {
		return StringUtils.hasText(request.resultType()) ? request.resultType() : properties.resolvedResultType();
	}

	private void addQueryParam(UriComponentsBuilder builder, String name, String value) {
		if (StringUtils.hasText(value)) {
			builder.queryParam(name, value);
		}
	}

	private String toRawTitleKeywordQueryValue(String value) {
		return value.trim().replace(" ", "+");
	}

	private void validateConfiguration() {
		if (!StringUtils.hasText(properties.baseUrl()) || !StringUtils.hasText(properties.recruitListPath())) {
			throw new AlioApiException("ALIO API baseUrl or recruitListPath is not configured.");
		}

		if (!properties.hasServiceKey()) {
			throw new AlioApiException("ALIO API serviceKey is not configured.");
		}
	}

	private AlioApiException buildAlioApiException(URI requestUri, Exception exception) {
		StringBuilder message = new StringBuilder("Failed to fetch recruitment list from ALIO API");
		String requestUriText = requestUri.toString();

		if (exception instanceof RestClientResponseException responseException) {
			message.append(" (status: ").append(responseException.getStatusCode()).append(")");
			message.append(". Request URI: ").append(requestUriText);

			String responseBody = responseException.getResponseBodyAsString();
			if (StringUtils.hasText(responseBody)) {
				message.append(". Response body: ").append(responseBody);
			}

			return new AlioApiException(
				message.toString(),
				exception,
				responseException.getStatusCode(),
				responseBody,
				requestUriText
			);
		}

		message.append(". Request URI: ").append(requestUriText);

		if (StringUtils.hasText(exception.getMessage())) {
			message.append(". Cause: ").append(exception.getMessage());
		}

		return new AlioApiException(message.toString(), exception, null, null, requestUriText);
	}
}
