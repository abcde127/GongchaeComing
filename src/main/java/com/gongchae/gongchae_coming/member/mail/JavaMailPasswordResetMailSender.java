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
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
			helper.setFrom(new InternetAddress(resolveFromAddress(), "공채왔어요", StandardCharsets.UTF_8.name()));
			helper.setTo(email);
			helper.setSubject("[공채왔어요] 비밀번호 재설정 인증번호");
			helper.setText(createPlainText(code), createHtmlText(code));
			javaMailSender.send(message);
		} catch (MessagingException | UnsupportedEncodingException exception) {
			throw new IllegalStateException("failed to create password reset verification mail", exception);
		}
	}

	private String createPlainText(String code) {
		return """
			공채왔어요 비밀번호 재설정 인증번호

			인증번호: %s

			인증번호는 5분 동안만 유효합니다.
			본인이 요청하지 않았다면 이 메일을 무시해주세요.
			""".formatted(code);
	}

	private String createHtmlText(String code) {
		return """
			<!doctype html>
			<html lang="ko">
			<head>
				<meta charset="UTF-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>공채왔어요 비밀번호 재설정 인증번호</title>
			</head>
			<body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,'Apple SD Gothic Neo','Malgun Gothic',sans-serif;color:#1f2937;">
				<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f4f6f8;">
					<tr>
						<td align="center" style="padding:40px 16px;">
							<table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;border-collapse:collapse;background:#ffffff;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;">
								<tr>
									<td style="padding:28px 32px 18px;border-bottom:1px solid #eef2f7;">
										<div style="font-size:14px;font-weight:700;color:#2563eb;margin-bottom:10px;">공채왔어요</div>
										<h1 style="margin:0;font-size:22px;line-height:1.4;font-weight:700;color:#111827;">비밀번호 재설정 인증번호</h1>
									</td>
								</tr>
								<tr>
									<td style="padding:30px 32px 8px;">
										<p style="margin:0 0 18px;font-size:15px;line-height:1.7;color:#374151;">
											비밀번호 재설정을 위해 아래 인증번호를 입력해주세요.
										</p>
										<div style="margin:0 0 22px;padding:22px 20px;background:#f8fafc;border:1px solid #dbeafe;border-radius:8px;text-align:center;">
											<div style="font-size:13px;font-weight:700;color:#2563eb;margin-bottom:8px;">인증번호</div>
											<div style="font-size:34px;line-height:1.2;font-weight:800;color:#111827;">%s</div>
										</div>
										<p style="margin:0 0 8px;font-size:14px;line-height:1.7;color:#4b5563;">
											인증번호는 <strong style="color:#2563eb;">5분 동안</strong>만 유효합니다.
										</p>
										<p style="margin:0;font-size:13px;line-height:1.7;color:#6b7280;">
											본인이 요청하지 않았다면 이 메일을 무시해주세요.
										</p>
									</td>
								</tr>
								<tr>
									<td style="padding:24px 32px 30px;">
										<div style="padding-top:18px;border-top:1px solid #eef2f7;font-size:12px;line-height:1.6;color:#9ca3af;">
											이 메일은 공채왔어요 비밀번호 재설정 요청에 의해 발송되었습니다.
										</div>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</body>
			</html>
			""".formatted(code);
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
