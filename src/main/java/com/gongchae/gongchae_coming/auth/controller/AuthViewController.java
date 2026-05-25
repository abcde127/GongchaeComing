package com.gongchae.gongchae_coming.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/signup")
	public String signup() {
		return "signup";
	}

	@GetMapping("/signup-complete")
	public String signupComplete() {
		return "signup-complete";
	}

	@GetMapping("/find-password")
	public String findPassword() {
		return "redirect:/reset-password";
	}

	@GetMapping("/reset-password")
	public String resetPassword() {
		return "reset-password";
	}

	@GetMapping("/mypage")
	public String mypage() {
		return "mypage";
	}
}
