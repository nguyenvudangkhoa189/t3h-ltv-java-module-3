package vn.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** CONTROLLER — redirect gốc {@code /} → {@code /movies}. */
@Controller
public class HomeController {

	@GetMapping("/")
	public String home() {
		// --- Trang mặc định của demo Bài 7 ---
		return "redirect:/movies";
	}

}
