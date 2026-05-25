package com.gongchae.gongchae_coming.member.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class JavaMailPasswordResetMailSender implements PasswordResetMailSender {

	private final JavaMailSender javaMailSender;

	@Value("${app.mail.from:no-reply@gongchae-coming.local}")
	private String from;

	@Override
	public void sendVerificationCode(String email, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(email);
		message.setSubject("[공채왔어요] 비밀번호 재설정 인증번호");
		message.setText("""
			비밀번호 재설정 인증번호는 %s 입니다.

			인증번호는 5분 동안만 유효합니다.
			본인이 요청하지 않았다면 이 메일을 무시해주세요.
			""".formatted(code));
		javaMailSender.send(message);
	}
}
