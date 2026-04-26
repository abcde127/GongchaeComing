package com.gongchae.gongchae_coming.global.exception;

import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

	private final ApiExceptionHandler handler = new ApiExceptionHandler();

	@Test
	void handleAlioApiExceptionIncludesAlioDebugProperties() {
		AlioApiException exception = new AlioApiException(
			"Failed to fetch recruitment list from ALIO API",
			null,
			HttpStatusCode.valueOf(500),
			"{\"resultCode\":\"30\"}",
			"https://opendata.alio.go.kr/new/v1/recruit/list.do?recrutPbancTtl=abc"
		);

		var problemDetail = handler.handleAlioApiException(exception);

		assertThat(problemDetail.getStatus()).isEqualTo(502);
		assertThat(problemDetail.getTitle()).isEqualTo("ALIO API request failed");
		assertThat(problemDetail.getProperties())
			.containsEntry("alioStatus", 500)
			.containsEntry("alioResponseBody", "{\"resultCode\":\"30\"}")
			.containsEntry("alioRequestUri", "https://opendata.alio.go.kr/new/v1/recruit/list.do?recrutPbancTtl=abc");
	}
}
