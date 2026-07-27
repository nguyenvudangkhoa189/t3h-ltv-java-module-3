package vn.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * CONFIG — Spring Security cho Mini Project.
 *
 * <p><b>Flow request:</b> Browser → SecurityFilterChain → (permit / login / chặn)
 * → Controller. Public {@code /movies/**} mở; {@code /admin/**} cần {@code ROLE_ADMIN}.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/** Bean hash mật khẩu (seed + so khớp khi login). */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Chuỗi filter chính: CSRF mặc định bật; formLogin + logout.
	 *
	 * @param http builder Security
	 * @return filter chain đã cấu hình
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// --- Ai được vào URL nào ---
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/",
								"/movies/**",
								"/css/**",
								"/js/**",
								"/img/**",
								"/fonts/**",
								"/videos/**",
								"/dashmin/**",
								"/login")
						.permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.anyRequest().permitAll())
				// --- Form login Thymeleaf; thành công → dashboard ---
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/admin/dashboard", true)
						.permitAll())
				// --- Logout hủy session ---
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll());
		return http.build();
	}

}
