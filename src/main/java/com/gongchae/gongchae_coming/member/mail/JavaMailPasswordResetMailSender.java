package com.gongchae.gongchae_coming.member.mail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class JavaMailPasswordResetMailSender implements PasswordResetMailSender {

	private final JavaMailSender javaMailSender;

	@Value("${app.mail.from:no-reply@gongchae-coming.local}")
	private String from;

	@Value("${spring.mail.host:}")
	private String mailHost;

	@Value("${spring.mail.username:}")
	private String mailUsername;

	@Override
	public void sendVerificationCode(String email, String code) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
			helper.setFrom(new InternetAddress(resolveFromAddress(), "공채왔어요", StandardCharsets.UTF_8.name()));
			helper.setTo(email);
			helper.setSubject("[공채왔어요] 비밀번호 재설정 인증번호");
			helper.setText("""
				비밀번호 재설정 인증번호는 %s 입니다.

				인증번호는 5분 동안만 유효합니다.
				본인이 요청하지 않았다면 이 메일을 무시해주세요.
				""".formatted(code), false);
			javaMailSender.send(message);
		} catch (MessagingException | UnsupportedEncodingException exception) {
			throw new IllegalStateException("failed to create password reset verification mail", exception);
		}
	}

	private String resolveFromAddress() {
		String usernameAddress = normalizeMailAddress(mailUsername);
		if (isNaverSmtp() && StringUtils.hasText(usernameAddress)) {
			return usernameAddress;
		}

		String configuredAddress = normalizeMailAddress(from);
		if (StringUtils.hasText(configuredAddress)) {
			return configuredAddress;
		}

		return usernameAddress;
	}

	private String normalizeMailAddress(String address) {
		if (!StringUtils.hasText(address)) {
			return address;
		}

		String trimmedAddress = address.trim();
		if (isNaverSmtp() && !trimmedAddress.contains("@")) {
			return trimmedAddress + "@naver.com";
		}

		return trimmedAddress;
	}

	private boolean isNaverSmtp() {
		return "smtp.naver.com".equalsIgnoreCase(mailHost);
	}
}
