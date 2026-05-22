package com.gongchae.gongchae_coming.member.service;

import com.gongchae.gongchae_coming.alio.repository.PublicInstitutionRepository;
import com.gongchae.gongchae_coming.member.domain.Member;
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
import com.gongchae.gongchae_coming.member.exception.DuplicateMemberException;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberService {

	private static final Set<String> RECRUITMENT_STATUSES = Set.of("scheduled", "active", "closed");
	private static final Set<String> REGIONS = Set.of(
		"R3010", "R3011", "R3012", "R3013", "R3014", "R3015", "R3016", "R3017", "R3018", "R3019",
		"R3020", "R3021", "R3022", "R3023", "R3024", "R3025", "R3026", "R3030"
	);
	private static final Set<String> CATEGORIES = Set.of("R2010", "R2020", "R2040");
	private static final Set<String> HIRE_TYPES = Set.of("R1010", "R1020", "R1030", "R1040", "R1050", "R1060", "R1070");
	private static final Set<String> NCS_CODES = Set.of(
		"R600001", "R600002", "R600003", "R600004", "R600005", "R600006", "R600007", "R600008", "R600009",
		"R600010", "R600011", "R600012", "R600013", "R600014", "R600015", "R600016", "R600017", "R600018",
		"R600019", "R600020", "R600021", "R600022", "R600023", "R600024", "R600025"
	);

	private final MemberRepository memberRepository;
	private final PublicInstitutionRepository publicInstitutionRepository;
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

	@Transactional(readOnly = true)
	public MemberJobPreferenceResponse getJobPreference(String email) {
		return MemberJobPreferenceResponse.from(findByEmail(email));
	}

	@Transactional(readOnly = true)
	public List<MemberJobPreferenceCompanyResponse> getJobPreferenceCompanies() {
		return publicInstitutionRepository.findAllByOrderByInstNmAsc().stream()
			.map(MemberJobPreferenceCompanyResponse::from)
			.toList();
	}

	@Transactional
	public MemberJobPreferenceResponse updateJobPreference(String email, MemberJobPreferenceRequest request) {
		Member member = findByEmail(email);

		member.updateJobPreference(
			normalizeText(request.searchKeyword()),
			joinAllowedPublicInstitutionCodes(request.companies()),
			joinAllowedValues(request.recruitmentStatuses(), RECRUITMENT_STATUSES, "invalid recruitment status"),
			joinAllowedValues(request.regions(), REGIONS, "invalid region"),
			joinAllowedValues(request.categories(), CATEGORIES, "invalid category"),
			joinAllowedValues(request.hireTypes(), HIRE_TYPES, "invalid hire type"),
			joinAllowedValues(request.ncsCodes(), NCS_CODES, "invalid ncs code")
		);

		return MemberJobPreferenceResponse.from(member);
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

	private String normalizeText(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String joinAllowedValues(List<String> values, Set<String> allowedValues, String errorMessage) {
		if (values == null || values.isEmpty()) {
			return null;
		}

		List<String> normalizedValues = values.stream()
			.filter(StringUtils::hasText)
			.map(String::trim)
			.distinct()
			.toList();

		if (!allowedValues.containsAll(normalizedValues)) {
			throw new IllegalArgumentException(errorMessage);
		}

		return normalizedValues.isEmpty() ? null : String.join(",", normalizedValues);
	}

	private String joinAllowedPublicInstitutionCodes(List<String> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}

		List<String> normalizedValues = values.stream()
			.filter(StringUtils::hasText)
			.map(String::trim)
			.distinct()
			.toList();

		if (normalizedValues.isEmpty()) {
			return null;
		}

		Set<String> existingCodes = new HashSet<>(
			publicInstitutionRepository.findByInstCdIn(normalizedValues).stream()
				.map(institution -> institution.getInstCd())
				.toList()
		);
		if (!existingCodes.containsAll(normalizedValues)) {
			throw new IllegalArgumentException("invalid company");
		}

		return String.join(",", normalizedValues);
	}

}
