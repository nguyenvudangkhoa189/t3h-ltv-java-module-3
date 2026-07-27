package vn.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.UserModel;
import vn.demo.repository.UserRepository;

/**
 * CONFIG — seed tài khoản admin lần đầu chạy app.
 *
 * <p><b>Flow:</b> App start → kiểm tra username trong MongoDB → chưa có thì
 * BCrypt password → lưu collection {@code users}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.username:admin}")
	private String username;

	@Value("${app.admin.password:admin123}")
	private String rawPassword;

	@Override
	public void run(ApplicationArguments args) {
		// --- Đã có admin → bỏ qua (idempotent) ---
		if (userRepository.findByUsername(username).isPresent()) {
			return;
		}

		// --- Tạo user ADMIN với password đã hash ---
		UserModel admin = new UserModel();
		admin.setUsername(username);
		admin.setPassword(passwordEncoder.encode(rawPassword));
		admin.setRole("ADMIN");
		admin.setEnabled(true);
		userRepository.save(admin);

		log.info("AdminUserSeeder: created user '{}' (change password in production)", username);
	}

}
