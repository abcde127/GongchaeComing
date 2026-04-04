package com.gongchae.gongchae_coming.alio.exception;

public class AlioApiException extends RuntimeException {

	public AlioApiException(String message) {
		super(message);
	}

	public AlioApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
