package com.gongchae.gongchae_coming.global.exception;

import com.gongchae.gongchae_coming.alio.exception.AlioApiException;
import com.gongchae.gongchae_coming.member.exception.DuplicateMemberException;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleRequestBodyValidationException(MethodArgumentNotValidException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setTitle("Invalid request body");
		problemDetail.setDetail(exception.getBindingResult().getAllErrors().stream()
			.findFirst()
			.map(error -> error.getDefaultMessage())
			.orElse("Request body validation failed."));
		return problemDetail;
	}

	@ExceptionHandler(DuplicateMemberException.class)
	public ProblemDetail handleDuplicateMemberException(DuplicateMemberException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problemDetail.setTitle("Member already exists");
		problemDetail.setDetail(exception.getMessage());
		return problemDetail;
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ProblemDetail handleMemberNotFoundException(MemberNotFoundException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problemDetail.setTitle("Member not found");
		problemDetail.setDetail(exception.getMessage());
		return problemDetail;
	}
}
