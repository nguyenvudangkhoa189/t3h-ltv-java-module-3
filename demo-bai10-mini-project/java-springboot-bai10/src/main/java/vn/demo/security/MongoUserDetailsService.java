package vn.demo.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.model.UserModel;
import vn.demo.repository.UserRepository;

/**
 * SECURITY — cầu nối MongoDB {@code users} ↔ Spring Security.
 *
 * <p>Đây là <b>nguồn xác thực duy nhất</b> khi login: không đọc
 * {@code spring.security.user.*} hay {@code app.admin.*} trong properties.</p>
 *
 * <p><b>Flow login:</b> POST /login → {@code DaoAuthenticationProvider}
 * gọi {@link #loadUserByUsername} → query Mongo → trả {@link UserDetails}
 * (username + BCrypt hash + roles) → {@code PasswordEncoder.matches} → tạo session.</p>
 */
@Service
@RequiredArgsConstructor
public class MongoUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// --- Đọc user từ Mongo collection users; không thấy → UsernameNotFoundException ---
		UserModel u = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		// --- Map sang UserDetails (roles("ADMIN") → authority ROLE_ADMIN) ---
		// password ở đây là BCrypt hash đã lưu trong DB — Security tự matches với raw từ form
		return User.withUsername(u.getUsername())
				.password(u.getPassword())
				.roles(u.getRole())
				.disabled(!u.isEnabled())
				.build();
	}

}
