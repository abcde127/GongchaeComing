package com.gongchae.gongchae_coming.member.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

class JavaMailPasswordResetMailSenderTest {

	@Test
	void sendVerificationCodeCreatesHtmlMail() throws Exception {
		CapturingJavaMailSender javaMailSender = new CapturingJavaMailSender();
		JavaMailPasswordResetMailSender mailSender = new JavaMailPasswordResetMailSender(javaMailSender);
		ReflectionTestUtils.setField(mailSender, "from", "sender@example.com");

		mailSender.sendVerificationCode("user@example.com", "123456");

		MimeMessage sentMessage = javaMailSender.sentMessage;
		assertThat(sentMessage).isNotNull();
		assertThat(sentMessage.getSubject()).isEqualTo("[공채왔어요] 비밀번호 재설정 인증번호");
		assertThat(sentMessage.getFrom()[0].toString()).contains("sender@example.com");
		assertThat(sentMessage.getContentType()).contains("multipart");
		assertThat(extractText(sentMessage.getContent())).contains("123456");
	}

	private static String extractText(Object content) throws MessagingException, IOException {
		if (content instanceof String text) {
			return text;
		}

		if (content instanceof Multipart multipart) {
			StringBuilder text = new StringBuilder();
			for (int index = 0; index < multipart.getCount(); index++) {
				BodyPart bodyPart = multipart.getBodyPart(index);
				text.append(extractText(bodyPart.getContent()));
			}
			return text.toString();
		}

		return "";
	}

	private static class CapturingJavaMailSender implements JavaMailSender {

		private final JavaMailSenderImpl delegate = new JavaMailSenderImpl();

		private MimeMessage sentMessage;

		@Override
		public MimeMessage createMimeMessage() {
			return delegate.createMimeMessage();
		}

		@Override
		public MimeMessage createMimeMessage(InputStream contentStream) {
			return delegate.createMimeMessage(contentStream);
		}

		@Override
		public void send(MimeMessage mimeMessage) {
			saveChanges(mimeMessage);
			this.sentMessage = mimeMessage;
		}

		@Override
		public void send(MimeMessage... mimeMessages) {
			this.sentMessage = mimeMessages[0];
		}

		@Override
		public void send(MimeMessagePreparator mimeMessagePreparator) {
			MimeMessage mimeMessage = createMimeMessage();
			try {
				mimeMessagePreparator.prepare(mimeMessage);
			} catch (Exception exception) {
				throw new IllegalStateException(exception);
			}
			saveChanges(mimeMessage);
			this.sentMessage = mimeMessage;
		}

		@Override
		public void send(MimeMessagePreparator... mimeMessagePreparators) {
			send(mimeMessagePreparators[0]);
		}

		@Override
		public void send(SimpleMailMessage simpleMessage) {
		}

		@Override
		public void send(SimpleMailMessage... simpleMessages) {
		}

		private void saveChanges(MimeMessage mimeMessage) {
			try {
				mimeMessage.saveChanges();
			} catch (MessagingException exception) {
				throw new IllegalStateException(exception);
			}
		}
	}
}
