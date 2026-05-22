package com.gongchae.gongchae_coming.alio.client;

import com.gongchae.gongchae_coming.alio.config.AlioApiProperties;
import com.gongchae.gongchae_coming.alio.dto.AlioRecruitmentListRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AlioRecruitmentClientTest {

	@Test
	void buildRequestUriForDebugKeepsServiceKeyAsConfigured() {
		var properties = new AlioApiProperties(
			"https://apis.data.go.kr/1051000/recruitment",
			"/list",
			"https://apis.data.go.kr/1051000/public_inst/list",
			"abc%2B%2F%3D",
			"json"
		);
		var client = new AlioRecruitmentClient(null, properties);

		String requestUri = client.buildRequestUriForDebug(emptyRequest());

		assertThat(requestUri).contains("serviceKey=abc%2B%2F%3D");
		assertThat(requestUri).doesNotContain("%252B");
	}

	@Test
	void buildRequestUriForDebugDoesNotEncodeRawServiceKeyCharacters() {
		var properties = new AlioApiProperties(
			"https://apis.data.go.kr/1051000/recruitment",
			"/list",
			"https://apis.data.go.kr/1051000/public_inst/list",
			"abc+/=",
			"json"
		);
		var client = new AlioRecruitmentClient(null, properties);

		String requestUri = client.buildRequestUriForDebug(emptyRequest());

		assertThat(requestUri).contains("serviceKey=abc+/=");
	}

	@Test
	void fetchRecruitmentsSendsGetRequest() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.build();
		var properties = new AlioApiProperties(
			"https://apis.data.go.kr/1051000/recruitment",
			"/list",
			"https://apis.data.go.kr/1051000/public_inst/list",
			"abc%2B%2F%3D",
			"json"
		);
		var client = new AlioRecruitmentClient(restClient, properties);

		server.expect(method(HttpMethod.GET))
			.andRespond(withSuccess("{\"resultCode\":\"00\"}", MediaType.APPLICATION_JSON));

		client.fetchRecruitments(emptyRequest());

		server.verify();
	}

	private AlioRecruitmentListRequest emptyRequest() {
		return new AlioRecruitmentListRequest(
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}
}
