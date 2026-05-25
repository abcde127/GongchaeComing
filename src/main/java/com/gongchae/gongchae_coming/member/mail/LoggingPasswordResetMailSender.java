package com.gongchae.gongchae_coming.member.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingPasswordResetMailSender implements PasswordResetMailSender {

	@Override
	public void sendVerificationCode(String email, String code) {
		log.info("Password reset verification code for {}: {}", email, code);
	}
}
