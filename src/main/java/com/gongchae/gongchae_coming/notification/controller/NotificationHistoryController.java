package com.gongchae.gongchae_coming.notification.controller;

import com.gongchae.gongchae_coming.notification.dto.NotificationHistoryResponse;
import com.gongchae.gongchae_coming.notification.service.NotificationHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/me/notifications/histories")
public class NotificationHistoryController {

	private final NotificationHistoryService notificationHistoryService;

	@GetMapping
	public List<NotificationHistoryResponse> getMyHistories(Authentication authentication) {
		return notificationHistoryService.getMyHistories(authentication.getName());
	}
}
