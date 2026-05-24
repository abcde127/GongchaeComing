package com.gongchae.gongchae_coming.notification.service;

import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import com.gongchae.gongchae_coming.notification.domain.NotificationHistory;
import com.gongchae.gongchae_coming.notification.domain.NotificationType;
import com.gongchae.gongchae_coming.notification.dto.NotificationHistoryResponse;
import com.gongchae.gongchae_coming.notification.repository.NotificationHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NotificationHistoryService {

	private static final int DEFAULT_HISTORY_LIMIT = 50;
	private static final int MAX_FAILURE_REASON_LENGTH = 1000;

	private final NotificationHistoryRepository notificationHistoryRepository;
	private final MemberRepository memberRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordSuccess(Member member, NotificationType type) {
		Member memberReference = memberRepository.getReferenceById(member.getId());
		notificationHistoryRepository.save(NotificationHistory.success(memberReference, type));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordFailure(Member member, NotificationType type, Exception exception) {
		Member memberReference = memberRepository.getReferenceById(member.getId());
		notificationHistoryRepository.save(NotificationHistory.failure(memberReference, type, failureReason(exception)));
	}

	@Transactional(readOnly = true)
	public List<NotificationHistoryResponse> getMyHistories(String email) {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new MemberNotFoundException("member not found"));

		return notificationHistoryRepository
			.findByMemberIdOrderBySentAtDescIdDesc(member.getId(), PageRequest.of(0, DEFAULT_HISTORY_LIMIT))
			.stream()
			.map(NotificationHistoryResponse::from)
			.toList();
	}

	private String failureReason(Exception exception) {
		if (exception == null) {
			return "unknown notification send failure";
		}

		String message = exception.getMessage();
		if (!StringUtils.hasText(message)) {
			message = exception.getClass().getSimpleName();
		}
		if (message.length() <= MAX_FAILURE_REASON_LENGTH) {
			return message;
		}
		return message.substring(0, MAX_FAILURE_REASON_LENGTH);
	}
}
