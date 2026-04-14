package com.gongchae.gongchae_coming.member.controller;

import com.gongchae.gongchae_coming.member.dto.MemberFindIdRequest;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdResponse;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordRequest;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordResponse;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

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
