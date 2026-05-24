package com.gongchae.gongchae_coming.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gongchae.gongchae_coming.alio.domain.PublicInstitution;
import com.gongchae.gongchae_coming.alio.repository.PublicInstitutionRepository;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdRequest;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdResponse;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceRequest;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceResponse;
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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PublicInstitutionRepository publicInstitutionRepository;

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

	@Test
	void isEmailAvailableReturnsTrueWhenEmailDoesNotExist() {
		assertThat(memberService.isEmailAvailable("new@example.com")).isTrue();
	}

	@Test
	void isEmailAvailableReturnsFalseWhenEmailExists() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		assertThat(memberService.isEmailAvailable("user@example.com")).isFalse();
	}

	@Test
	void findIdReturnsMemberIdByEmail() {
		MemberSignupResponse signupResponse = memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		MemberFindIdResponse response = memberService.findId(new MemberFindIdRequest("user@example.com"));

		assertThat(response.memberId()).isEqualTo(signupResponse.id());
		assertThat(response.email()).isEqualTo("user@example.com");
		assertThat(response.nickname()).isEqualTo("gongchae");
	}

	@Test
	void findIdRejectsUnknownEmail() {
		assertThatThrownBy(() -> memberService.findId(new MemberFindIdRequest("missing@example.com")))
			.isInstanceOf(MemberNotFoundException.class)
			.hasMessage("member not found");
	}

	@Test
	void resetPasswordChangesPasswordWithEncodedPassword() {
		MemberSignupResponse signupResponse = memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		MemberResetPasswordResponse response = memberService.resetPassword(new MemberResetPasswordRequest(
			"user@example.com",
			"gongchae",
			"newpassword1"
		));

		Member savedMember = memberRepository.findById(signupResponse.id()).orElseThrow();
		assertThat(response.memberId()).isEqualTo(signupResponse.id());
		assertThat(response.email()).isEqualTo("user@example.com");
		assertThat(response.nickname()).isEqualTo("gongchae");
		assertThat(savedMember.getPassword()).isNotEqualTo("newpassword1");
		assertThat(passwordEncoder.matches("newpassword1", savedMember.getPassword())).isTrue();
		assertThat(passwordEncoder.matches("password1", savedMember.getPassword())).isFalse();
	}

	@Test
	void resetPasswordRejectsUnknownMember() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		assertThatThrownBy(() -> memberService.resetPassword(new MemberResetPasswordRequest(
			"user@example.com",
			"wrongnickname",
			"newpassword1"
		))).isInstanceOf(MemberNotFoundException.class)
			.hasMessage("member not found");
	}

	@Test
	void getProfileReturnsMemberProfile() {
		MemberSignupResponse signupResponse = memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		MemberProfileResponse response = memberService.getProfile("user@example.com");

		assertThat(response.id()).isEqualTo(signupResponse.id());
		assertThat(response.email()).isEqualTo("user@example.com");
		assertThat(response.nickname()).isEqualTo("gongchae");
	}

	@Test
	void updateNicknameChangesNicknameWithoutPassword() {
		MemberSignupResponse signupResponse = memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		MemberProfileResponse response = memberService.updateNickname(
			"user@example.com",
			new MemberNicknameUpdateRequest("newgongchae")
		);

		Member savedMember = memberRepository.findById(signupResponse.id()).orElseThrow();
		assertThat(response.nickname()).isEqualTo("newgongchae");
		assertThat(savedMember.getNickname()).isEqualTo("newgongchae");
	}

	@Test
	void updateNicknameRejectsDuplicateNicknameOwnedByAnotherMember() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));
		memberService.signup(new MemberSignupRequest(
			"other@example.com",
			"othergongchae",
			"password1"
		));

		assertThatThrownBy(() -> memberService.updateNickname(
			"user@example.com",
			new MemberNicknameUpdateRequest("othergongchae")
		)).isInstanceOf(DuplicateMemberException.class)
			.hasMessage("nickname already exists");
	}

	@Test
	void updatePasswordChangesPasswordWithCurrentPassword() {
		MemberSignupResponse signupResponse = memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		MemberProfileResponse response = memberService.updatePassword(
			"user@example.com",
			new MemberPasswordUpdateRequest("password1", "newpassword1")
		);

		Member savedMember = memberRepository.findById(signupResponse.id()).orElseThrow();
		assertThat(response.email()).isEqualTo("user@example.com");
		assertThat(passwordEncoder.matches("newpassword1", savedMember.getPassword())).isTrue();
		assertThat(passwordEncoder.matches("password1", savedMember.getPassword())).isFalse();
	}

	@Test
	void updatePasswordRejectsWrongCurrentPassword() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		assertThatThrownBy(() -> memberService.updatePassword(
			"user@example.com",
			new MemberPasswordUpdateRequest("wrongpassword", "newpassword1")
		)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("current password does not match");
	}

	@Test
	void updateJobPreferenceStoresAllowedFilters() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));
		savePublicInstitution("C0001", "88관광개발(주)");
		savePublicInstitution("C0002", "한국가스공사");

		MemberJobPreferenceResponse response = memberService.updateJobPreference(
			"user@example.com",
			new MemberJobPreferenceRequest(
				" 전산 ",
				List.of("C0001", "C0001", "C0002"),
				List.of("active", "active", "scheduled"),
				List.of("R3010"),
				List.of("R2010"),
				List.of("R1010"),
				List.of("R600020")
			)
		);

		assertThat(response.searchKeyword()).isEqualTo("전산");
		assertThat(response.companies()).containsExactly("C0001", "C0002");
		assertThat(response.recruitmentStatuses()).containsExactly("active", "scheduled");
		assertThat(response.regions()).containsExactly("R3010");
		assertThat(response.categories()).containsExactly("R2010");
		assertThat(response.hireTypes()).containsExactly("R1010");
		assertThat(response.ncsCodes()).containsExactly("R600020");
	}

	@Test
	void updateJobPreferenceRejectsInvalidFilterValue() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		assertThatThrownBy(() -> memberService.updateJobPreference(
			"user@example.com",
			new MemberJobPreferenceRequest(
				null,
				List.of(),
				List.of("active"),
				List.of("INVALID"),
				List.of(),
				List.of(),
				List.of()
			)
		)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("invalid region");
	}

	@Test
	void updateJobPreferenceRejectsInvalidCompany() {
		memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));

		assertThatThrownBy(() -> memberService.updateJobPreference(
			"user@example.com",
			new MemberJobPreferenceRequest(
				null,
				List.of("INVALID"),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				List.of()
			)
		)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("invalid company");
	}

	private void savePublicInstitution(String instCd, String instNm) {
		ObjectNode item = OBJECT_MAPPER.createObjectNode();
		item.put("instCd", instCd);
		item.put("instNm", instNm);
		publicInstitutionRepository.save(PublicInstitution.from(item, java.time.LocalDateTime.now()));
	}
}
