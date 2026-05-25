package com.gongchae.gongchae_coming.alio.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RecruitmentViewController {

	@GetMapping("/")
	public String home(Authentication authentication, Model model) {
		addLoginStatus(authentication, model);
		return "home";
	}

	@GetMapping("/recruitments")
	public String recruitments(Authentication authentication, Model model) {
		addLoginStatus(authentication, model);
		return "recruitments";
	}

	@GetMapping("/statistics")
	public String statistics(Authentication authentication, Model model) {
		addLoginStatus(authentication, model);
		return "statistics";
	}

	@GetMapping("/recruitments/{recruitmentId}/redirect")
	public String recruitmentRedirectPage(
		@PathVariable String recruitmentId,
		Authentication authentication,
		Model model
	) {
		addLoginStatus(authentication, model);
		model.addAttribute("recruitmentId", recruitmentId);
		return "recruitment-redirect";
	}

	private void addLoginStatus(Authentication authentication, Model model) {
		boolean isLoggedIn = authentication != null
			&& authentication.isAuthenticated()
			&& !(authentication instanceof AnonymousAuthenticationToken);

		model.addAttribute("isLoggedIn", isLoggedIn);
	}
}
