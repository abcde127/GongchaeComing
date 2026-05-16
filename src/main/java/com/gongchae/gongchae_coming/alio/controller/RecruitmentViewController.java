package com.gongchae.gongchae_coming.alio.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecruitmentViewController {

	public static final String REFRESH_ALIO_RECRUITMENTS_SESSION_KEY = "refreshAlioRecruitmentsOnNextListRequest";

	@GetMapping("/")
	public String home() {
		return "redirect:/recruitments";
	}

	@GetMapping("/recruitments")
	public String recruitments(HttpSession session) {
		session.setAttribute(REFRESH_ALIO_RECRUITMENTS_SESSION_KEY, true);
		return "recruitments";
	}
}
