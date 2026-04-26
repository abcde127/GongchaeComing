package com.gongchae.gongchae_coming.alio.exception;

import org.springframework.http.HttpStatusCode;

public class AlioApiException extends RuntimeException {

	private final HttpStatusCode alioStatusCode;
	private final String alioResponseBody;
	private final String alioRequestUri;

	public AlioApiException(String message) {
		this(message, null, null, null, null);
	}

	public AlioApiException(String message, Throwable cause) {
		this(message, cause, null, null, null);
	}

	public AlioApiException(
		String message,
		Throwable cause,
		HttpStatusCode alioStatusCode,
		String alioResponseBody,
		String alioRequestUri
	) {
		super(message, cause);
		this.alioStatusCode = alioStatusCode;
		this.alioResponseBody = alioResponseBody;
		this.alioRequestUri = alioRequestUri;
	}

	public HttpStatusCode getAlioStatusCode() {
		return alioStatusCode;
	}

	public String getAlioResponseBody() {
		return alioResponseBody;
	}

	public String getAlioRequestUri() {
		return alioRequestUri;
	}
}
