package com.gongchae.gongchae_coming.member.controller;

import com.gongchae.gongchae_coming.member.dto.MemberEmailAvailabilityResponse;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdRequest;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdResponse;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceCompanyResponse;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceRequest;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceResponse;
import com.gongchae.gongchae_coming.member.dto.MemberNicknameUpdateRequest;
import com.gongchae.gongchae_coming.member.dto.MemberPasswordUpdateRequest;
import com.gongchae.gongchae_coming.member.dto.MemberProfileResponse;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordRequest;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordResponse;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

	@GetMapping("/me")
	public MemberProfileResponse getProfile(Authentication authentication) {
		return memberService.getProfile(authentication.getName());
	}

	@PatchMapping("/me/nickname")
	public MemberProfileResponse updateNickname(
		Authentication authentication,
		@Valid @RequestBody MemberNicknameUpdateRequest request
	) {
		return memberService.updateNickname(authentication.getName(), request);
	}

	@PatchMapping("/me/password")
	public MemberProfileResponse updatePassword(
		Authentication authentication,
		@Valid @RequestBody MemberPasswordUpdateRequest request
	) {
		return memberService.updatePassword(authentication.getName(), request);
	}

	@GetMapping("/me/job-preference")
	public MemberJobPreferenceResponse getJobPreference(Authentication authentication) {
		return memberService.getJobPreference(authentication.getName());
	}

	@GetMapping("/me/job-preference/companies")
	public List<MemberJobPreferenceCompanyResponse> getJobPreferenceCompanies() {
		return memberService.getJobPreferenceCompanies();
	}

	@PatchMapping("/me/job-preference")
	public MemberJobPreferenceResponse updateJobPreference(
		Authentication authentication,
		@Valid @RequestBody MemberJobPreferenceRequest request
	) {
		return memberService.updateJobPreference(authentication.getName(), request);
	}
}
