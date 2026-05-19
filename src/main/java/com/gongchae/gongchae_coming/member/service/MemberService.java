package com.gongchae.gongchae_coming.member.service;

import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdRequest;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdResponse;
import com.gongchae.gongchae_coming.member.dto.MemberNicknameUpdateRequest;
import com.gongchae.gongchae_coming.member.dto.MemberPasswordUpdateRequest;
import com.gongchae.gongchae_coming.member.dto.MemberProfileResponse;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordRequest;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordResponse;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.exception.DuplicateMemberException;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public MemberSignupResponse signup(MemberSignupRequest request) {
		validateDuplicateMember(request);

		Member member = Member.create(
			request.email(),
			request.nickname(),
			passwordEncoder.encode(request.password())
		);

		return MemberSignupResponse.from(memberRepository.save(member));
	}

	@Transactional(readOnly = true)
	public boolean isEmailAvailable(String email) {
		return !memberRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public MemberFindIdResponse findId(MemberFindIdRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new MemberNotFoundException("member not found"));

		return MemberFindIdResponse.from(member);
	}

	@Transactional
	public MemberResetPasswordResponse resetPassword(MemberResetPasswordRequest request) {
		Member member = memberRepository.findByEmailAndNickname(request.email(), request.nickname())
			.orElseThrow(() -> new MemberNotFoundException("member not found"));

		member.resetPassword(passwordEncoder.encode(request.newPassword()));

		return MemberResetPasswordResponse.from(member);
	}

	@Transactional(readOnly = true)
	public MemberProfileResponse getProfile(String email) {
		Member member = findByEmail(email);
		return MemberProfileResponse.from(member);
	}

	@Transactional
	public MemberProfileResponse updateNickname(String email, MemberNicknameUpdateRequest request) {
		Member member = findByEmail(email);

		if (memberRepository.existsByNicknameAndIdNot(request.nickname(), member.getId())) {
			throw new DuplicateMemberException("nickname already exists");
		}

		member.updateNickname(request.nickname());

		return MemberProfileResponse.from(member);
	}

	@Transactional
	public MemberProfileResponse updatePassword(String email, MemberPasswordUpdateRequest request) {
		Member member = findByEmail(email);

		if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
			throw new IllegalArgumentException("current password does not match");
		}

		member.resetPassword(passwordEncoder.encode(request.newPassword()));

		return MemberProfileResponse.from(member);
	}

	private void validateDuplicateMember(MemberSignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new DuplicateMemberException("email already exists");
		}

		if (memberRepository.existsByNickname(request.nickname())) {
			throw new DuplicateMemberException("nickname already exists");
		}
	}

	private Member findByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new MemberNotFoundException("member not found"));
	}
}
