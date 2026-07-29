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
 * <h2>Tổng quan</h2>
 * <p>Mỗi HTTP request đi qua {@link SecurityFilterChain} <b>trước</b> khi tới Controller.
 * Tài khoản <b>không</b> lấy từ {@code application.properties} / in-memory mặc định của Boot —
 * mà từ MongoDB collection {@code users} qua bean {@code MongoUserDetailsService}
 * (implement {@code UserDetailsService}). Boot tự gắn bean đó vào
 * {@code AuthenticationManager} khi chỉ có một {@code UserDetailsService}.</p>
 *
 * <h2>Cơ chế LOGIN (form login)</h2>
 * <ol>
 *   <li>User mở {@code GET /login} → {@code LoginController} trả {@code login.html}.</li>
 *   <li>User submit form {@code POST /login} với field chuẩn {@code username}, {@code password}
 *       (+ CSRF token do Thymeleaf {@code th:action} nhúng).</li>
 *   <li>{@code UsernamePasswordAuthenticationFilter} (nằm trong filter chain) bắt POST /login,
 *       đọc username/password, tạo {@code UsernamePasswordAuthenticationToken} (chưa authenticated).</li>
 *   <li>{@code AuthenticationManager} / {@code DaoAuthenticationProvider}:
 *       <ul>
 *         <li>Gọi {@code UserDetailsService.loadUserByUsername(username)}
 *             → {@code MongoUserDetailsService} query Mongo {@code users}.</li>
 *         <li>Không tìm thấy → login fail → redirect {@code /login?error}.</li>
 *         <li>Tìm thấy → lấy BCrypt hash từ document →
 *             {@code PasswordEncoder.matches(rawPassword, hashTrongDb)}.</li>
 *         <li>Sai mật khẩu / {@code enabled=false} → fail → {@code /login?error}.</li>
 *       </ul>
 *   </li>
 *   <li>Đúng → tạo {@code Authentication} đã authenticated, lưu vào
 *       {@code SecurityContext} (thường gắn với HTTP session) → browser nhận cookie
 *       {@code JSESSIONID}.</li>
 *   <li>Redirect theo {@code defaultSuccessUrl("/admin/dashboard", true)}.</li>
 *   <li>Request sau kèm cookie → Security đọc session → biết đã login + có
 *       {@code ROLE_ADMIN} → cho vào {@code /admin/**}.</li>
 * </ol>
 *
 * <h2>Cơ chế LOGOUT</h2>
 * <ol>
 *   <li>Form {@code POST /logout} (bắt buộc method POST khi CSRF bật — xem sidebar admin).</li>
 *   <li>{@code LogoutFilter} xử lý: invalidate HTTP session, xóa
 *       {@code SecurityContext}, xóa remember-me (nếu có).</li>
 *   <li>Redirect {@code /login?logout} → trang login hiện thông báo đã đăng xuất.</li>
 *   <li>Request tới {@code /admin/**} sau đó → không còn session → 302 về {@code /login}.</li>
 * </ol>
 *
 * <h2>Authorization (sau khi đã biết “bạn là ai”)</h2>
 * <ul>
 *   <li>Public ({@code /movies/**}, static, {@code /login}) → {@code permitAll()}.</li>
 *   <li>{@code /admin/**} → {@code hasRole("ADMIN")} (= authority {@code ROLE_ADMIN}).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * Bean hash / so khớp mật khẩu.
	 * <p>Dùng khi <b>seed</b> ({@code encode}) và khi <b>login</b> ({@code matches}).
	 * DaoAuthenticationProvider tự inject bean này.</p>
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Chuỗi filter chính: CSRF mặc định bật; authorize + formLogin + logout.
	 *
	 * @param http builder Security
	 * @return filter chain đã cấu hình
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// --- 1) Authorization: URL nào cần thẻ nào ---
				.authorizeHttpRequests(auth -> auth
						// Sảnh công cộng + asset + trang login: không cần authentication
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
						// Phòng admin: cần ROLE_ADMIN (role trong Mongo = "ADMIN")
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// Mọi URL khác còn lại: phải đã login (phòng thủ)
						.anyRequest().authenticated())

				// --- 2) Form login: kích hoạt UsernamePasswordAuthenticationFilter ---
				// loginPage: GET trang form tự viết (không dùng trang mặc định của Security)
				// loginProcessingUrl mặc định = "/login" (POST) — khớp th:action="@{/login}"
				// defaultSuccessUrl(..., true): luôn về dashboard sau login (bỏ qua SavedRequest)
				// User/pass được xác thực bằng MongoUserDetailsService + BCryptPasswordEncoder
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/admin/dashboard", true)
						.permitAll())

				// --- 3) Logout: kích hoạt LogoutFilter trên POST /logout ---
				// invalidateHttpSession mặc định = true; clearAuthentication = true
				// CSRF: form logout phải POST + token (Thymeleaf th:action tự thêm)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll());

		return http.build();
	}

}
