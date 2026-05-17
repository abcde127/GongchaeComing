package com.gongchae.gongchae_coming.alio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecruitmentViewController {

	@GetMapping("/")
	public String home() {
		return "redirect:/recruitments";
	}

	@GetMapping("/recruitments")
	public String recruitments() {
		return "recruitments";
	}
}
