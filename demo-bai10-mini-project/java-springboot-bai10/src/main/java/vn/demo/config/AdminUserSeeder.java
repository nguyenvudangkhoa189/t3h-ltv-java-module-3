package vn.demo.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.UserModel;
import vn.demo.repository.UserRepository;

/**
 * CONFIG — seed tài khoản admin lần đầu chạy app vào MongoDB {@code users}.
 *
 * <p><b>Vì sao không để user/pass trong {@code application.properties}?</b>
 * Properties dễ bị commit lên git; nguồn xác thực thật sự phải là document trong Mongo.
 * Seeder chỉ bootstrap lần đầu (idempotent) — sau đó login đọc từ DB qua
 * {@code MongoUserDetailsService}.</p>
 *
 * <p><b>Flow:</b> App start → kiểm tra username trong MongoDB → chưa có thì
 * BCrypt password → lưu collection {@code users}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

	/** Chỉ dùng khi seed lần đầu — không đọc từ properties. */
	private static final String DEFAULT_ADMIN_USERNAME = "admin";

	/** Plain text chỉ tồn tại trong bộ nhớ lúc encode; DB lưu BCrypt hash. */
	private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(ApplicationArguments args) {
		// --- Đã có admin trong Mongo → bỏ qua (idempotent) ---
		if (userRepository.findByUsername(DEFAULT_ADMIN_USERNAME).isPresent()) {
			log.info("AdminUserSeeder: user '{}' already exists in MongoDB — skip", DEFAULT_ADMIN_USERNAME);
			return;
		}

		// --- Tạo user ADMIN với password đã hash, lưu collection users ---
		UserModel admin = new UserModel();
		admin.setUsername(DEFAULT_ADMIN_USERNAME);
		admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
		admin.setRole("ADMIN");
		admin.setEnabled(true);
		userRepository.save(admin);

		log.info("AdminUserSeeder: created MongoDB user '{}' (BCrypt). Change password in production.",
				DEFAULT_ADMIN_USERNAME);
	}

}
