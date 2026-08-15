package com.tesda.banking.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeController {

	@GetMapping
	public Map<String, String> home() {
		return Map.of(
				"application", "POR Banking System",
				"meaning", "Palaging Overtime si Rodney",
				"tagline", "Kung walang resibo, baka drawing lang ang budget.",
				"status", "running");
	}
}
