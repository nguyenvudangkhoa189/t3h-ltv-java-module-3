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
 * <p><b>Flow login:</b> POST /login → Security gọi {@link #loadUserByUsername}
 * → lấy hash + role → {@code PasswordEncoder.matches} → tạo session.</p>
 */
@Service
@RequiredArgsConstructor
public class MongoUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// --- Đọc user từ Mongo; không thấy → UsernameNotFoundException ---
		UserModel u = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		// --- Map sang UserDetails (roles("ADMIN") → ROLE_ADMIN) ---
		return User.withUsername(u.getUsername())
				.password(u.getPassword())
				.roles(u.getRole())
				.disabled(!u.isEnabled())
				.build();
	}

}
