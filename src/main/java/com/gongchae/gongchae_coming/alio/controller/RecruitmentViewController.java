package com.gongchae.gongchae_coming.alio.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecruitmentViewController {

	@GetMapping("/")
	public String home() {
		return "redirect:/recruitments";
	}

	@GetMapping("/recruitments")
	public String recruitments(Authentication authentication, Model model) {
		boolean isLoggedIn = authentication != null
			&& authentication.isAuthenticated()
			&& !(authentication instanceof AnonymousAuthenticationToken);

		model.addAttribute("isLoggedIn", isLoggedIn);
		return "recruitments";
	}
}
