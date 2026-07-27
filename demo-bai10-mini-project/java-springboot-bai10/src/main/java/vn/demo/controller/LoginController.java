package vn.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CONTROLLER — trang login (GET).
 *
 * <p><b>Flow:</b> GET /login → {@code login.html}. POST /login do Spring Security xử lý
 * (không viết handler POST ở đây).</p>
 */
@Controller
public class LoginController {

	@GetMapping("/login")
	public String login() {
		return "login";
	}

}
