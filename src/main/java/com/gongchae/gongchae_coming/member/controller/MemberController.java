package com.gongchae.gongchae_coming.member.controller;

import com.gongchae.gongchae_coming.member.dto.MemberEmailAvailabilityResponse;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdRequest;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdResponse;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordRequest;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordResponse;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/email-availability")
	public MemberEmailAvailabilityResponse checkEmailAvailability(
		@RequestParam
		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		@Size(max = 100, message = "email must be 100 characters or less")
		String email
	) {
		return new MemberEmailAvailabilityResponse(email, memberService.isEmailAvailable(email));
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public MemberSignupResponse signup(@Valid @RequestBody MemberSignupRequest request) {
		return memberService.signup(request);
	}

	@PostMapping("/find-id")
	public MemberFindIdResponse findId(@Valid @RequestBody MemberFindIdRequest request) {
		return memberService.findId(request);
	}

	@PostMapping("/reset-password")
	public MemberResetPasswordResponse resetPassword(@Valid @RequestBody MemberResetPasswordRequest request) {
		return memberService.resetPassword(request);
	}
}
