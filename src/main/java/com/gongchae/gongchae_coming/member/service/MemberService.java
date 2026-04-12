package com.gongchae.gongchae_coming.member.service;

import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.exception.DuplicateMemberException;
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

	private void validateDuplicateMember(MemberSignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new DuplicateMemberException("email already exists");
		}

		if (memberRepository.existsByNickname(request.nickname())) {
			throw new DuplicateMemberException("nickname already exists");
		}
	}
}
