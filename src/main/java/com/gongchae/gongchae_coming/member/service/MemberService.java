package com.gongchae.gongchae_coming.member.service;

import com.gongchae.gongchae_coming.alio.repository.PublicInstitutionRepository;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.domain.PasswordResetVerification;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdRequest;
import com.gongchae.gongchae_coming.member.dto.MemberFindIdResponse;
import com.gongchae.gongchae_coming.member.dto.MemberFavoriteReminderRequest;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceCompanyResponse;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceRequest;
import com.gongchae.gongchae_coming.member.dto.MemberJobPreferenceResponse;
import com.gongchae.gongchae_coming.member.dto.MemberNicknameUpdateRequest;
import com.gongchae.gongchae_coming.member.dto.MemberPasswordUpdateRequest;
import com.gongchae.gongchae_coming.member.dto.MemberPasswordVerificationRequest;
import com.gongchae.gongchae_coming.member.dto.MemberProfileResponse;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordRequest;
import com.gongchae.gongchae_coming.member.dto.MemberResetPasswordResponse;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.dto.PasswordResetCodeRequest;
import com.gongchae.gongchae_coming.member.dto.PasswordResetCodeVerifyRequest;
import com.gongchae.gongchae_coming.member.dto.PasswordResetVerificationResponse;
import com.gongchae.gongchae_coming.member.exception.DuplicateMemberException;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.mail.PasswordResetMailSender;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import com.gongchae.gongchae_coming.member.repository.PasswordResetVerificationRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
	private static final int PASSWORD_RESET_CODE_BOUND = 1_000_000;
	private static final int PASSWORD_RESET_CODE_LENGTH = 6;
	private static final int PASSWORD_RESET_EXPIRATION_MINUTES = 5;
	private static final int PASSWORD_RESET_RESEND_INTERVAL_MINUTES = 1;
	private static final String PASSWORD_RESET_FAILURE_MESSAGE = "verification code is invalid or expired";
	private static final Set<String> NCS_CODES = Set.of(
		"R600001", "R600002", "R600003", "R600004", "R600005", "R600006", "R600007", "R600008", "R600009",
		"R600010", "R600011", "R600012", "R600013", "R600014", "R600015", "R600016", "R600017", "R600018",
		"R600019", "R600020", "R600021", "R600022", "R600023", "R600024", "R600025"
	);

	private final MemberRepository memberRepository;
	private final PasswordResetVerificationRepository passwordResetVerificationRepository;
	private final PublicInstitutionRepository publicInstitutionRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetMailSender passwordResetMailSender;
	private final SecureRandom secureRandom = new SecureRandom();

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
	public PasswordResetVerificationResponse requestPasswordResetCode(PasswordResetCodeRequest request) {
		String email = normalizeEmail(request.email());
		LocalDateTime now = LocalDateTime.now();

		passwordResetVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
			.filter(verification -> verification.getCreatedAt()
				.isAfter(now.minusMinutes(PASSWORD_RESET_RESEND_INTERVAL_MINUTES)))
			.ifPresent(verification -> {
				throw new IllegalArgumentException("verification code can be requested once per minute");
			});

		passwordResetVerificationRepository.findActiveByEmail(email)
			.forEach(verification -> verification.invalidate(now));

		String code = generateVerificationCode();
		PasswordResetVerification verification = PasswordResetVerification.create(
			email,
			passwordEncoder.encode(code),
			now.plusMinutes(PASSWORD_RESET_EXPIRATION_MINUTES)
		);
		passwordResetVerificationRepository.save(verification);

		if (memberRepository.existsByEmail(email)) {
			passwordResetMailSender.sendVerificationCode(email, code);
		}

		return PasswordResetVerificationResponse.codeRequested();
	}

	@Transactional
	public PasswordResetVerificationResponse verifyPasswordResetCode(PasswordResetCodeVerifyRequest request) {
		LocalDateTime now = LocalDateTime.now();
		verifyPasswordResetCode(normalizeEmail(request.email()), request.code(), now).verify(now);
		return PasswordResetVerificationResponse.codeVerified();
	}

	@Transactional
	public MemberResetPasswordResponse resetPassword(MemberResetPasswordRequest request) {
		String email = normalizeEmail(request.email());
		LocalDateTime now = LocalDateTime.now();
		PasswordResetVerification verification = verifyPasswordResetCode(email, request.code(), now);

		Member member = memberRepository.findByEmail(email)
			.orElse(null);
		if (member == null) {
			verification.use(now);
			return MemberResetPasswordResponse.completed();
		}

		if (passwordEncoder.matches(request.newPassword(), member.getPassword())) {
			throw new IllegalArgumentException("new password must be different from current password");
		}

		member.resetPassword(passwordEncoder.encode(request.newPassword()));
		verification.use(now);

		return MemberResetPasswordResponse.completed();
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
	public void verifyPassword(String email, MemberPasswordVerificationRequest request) {
		Member member = findByEmail(email);

		if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
			throw new IllegalArgumentException("current password does not match");
		}
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

	@Transactional
	public MemberProfileResponse updateFavoriteReminder(String email, MemberFavoriteReminderRequest request) {
		Member member = findByEmail(email);
		member.updateFavoriteReminder(request.enabled(), LocalTime.parse(request.reminderTime()));
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

	private PasswordResetVerification verifyPasswordResetCode(String email, String code, LocalDateTime now) {
		PasswordResetVerification verification = passwordResetVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
			.orElseThrow(() -> new IllegalArgumentException(PASSWORD_RESET_FAILURE_MESSAGE));

		if (verification.isVerifiedAndUsable(now) && passwordEncoder.matches(code, verification.getCodeHash())) {
			return verification;
		}

		if (!verification.canVerify(now)) {
			throw new IllegalArgumentException(PASSWORD_RESET_FAILURE_MESSAGE);
		}

		if (!passwordEncoder.matches(code, verification.getCodeHash())) {
			verification.increaseAttemptCount();
			throw new IllegalArgumentException(PASSWORD_RESET_FAILURE_MESSAGE);
		}

		return verification;
	}

	private String generateVerificationCode() {
		return String.format("%0" + PASSWORD_RESET_CODE_LENGTH + "d", secureRandom.nextInt(PASSWORD_RESET_CODE_BOUND));
	}

	private String normalizeEmail(String email) {
		return email.trim();
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
