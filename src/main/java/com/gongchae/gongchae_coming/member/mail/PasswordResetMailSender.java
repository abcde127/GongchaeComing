package com.gongchae.gongchae_coming.member.mail;

public interface PasswordResetMailSender {

	void sendVerificationCode(String email, String code);
}
