package vn.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * MODEL — tài khoản đăng nhập admin (collection {@code users}).
 *
 * <p>Password luôn là BCrypt hash. {@code role=ADMIN} → authority {@code ROLE_ADMIN}.</p>
 */
@Data
@Document(collection = "users")
public class UserModel {

	@Id
	private String id;

	@Indexed(unique = true)
	private String username;

	/** BCrypt hash — không lưu plain text. */
	private String password;

	/** Ví dụ {@code ADMIN}. */
	private String role;

	private boolean enabled = true;

}
