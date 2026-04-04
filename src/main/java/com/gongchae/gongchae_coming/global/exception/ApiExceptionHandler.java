package com.gongchae.gongchae_coming.global.exception;

import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(AlioApiException.class)
	public ProblemDetail handleAlioApiException(AlioApiException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
		problemDetail.setTitle("ALIO API request failed");
		problemDetail.setDetail(exception.getMessage());
		return problemDetail;
	}

	@ExceptionHandler(BindException.class)
	public ProblemDetail handleValidationException(BindException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setTitle("Invalid request parameter");
		problemDetail.setDetail(exception.getBindingResult().getAllErrors().stream()
			.findFirst()
			.map(error -> error.getDefaultMessage())
			.orElse("Request parameter validation failed."));
		return problemDetail;
	}
}
