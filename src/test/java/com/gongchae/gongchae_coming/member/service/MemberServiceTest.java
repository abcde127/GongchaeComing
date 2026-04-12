package com.gongchae.gongchae_coming.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.exception.DuplicateMemberException;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void signupCreatesMemberWithEncodedPassword() {
		MemberSignupRequest request = new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		);

		MemberSignupResponse response = memberService.signup(request);

		Member savedMember = memberRepository.findById(response.id()).orElseThrow();
		assertThat(savedMember.getEmail()).isEqualTo("user@example.com");
		assertThat(savedMember.getNickname()).isEqualTo("gongchae");
		assertThat(savedMember.getPassword()).isNotEqualTo("password1");
		assertThat(passwordEncoder.matches("password1", savedMember.getPassword())).isTrue();
	}

	@Test
	void signupRejectsDuplicateEmail() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		assertThatThrownBy(() -> memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae2",
			"password1"
		))).isInstanceOf(DuplicateMemberException.class)
			.hasMessage("email already exists");
	}

	@Test
	void signupRejectsDuplicateNickname() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		assertThatThrownBy(() -> memberService.signup(new MemberSignupRequest(
			"user2@example.com",
			"gongchae",
			"password1"
		))).isInstanceOf(DuplicateMemberException.class)
			.hasMessage("nickname already exists");
	}
}
