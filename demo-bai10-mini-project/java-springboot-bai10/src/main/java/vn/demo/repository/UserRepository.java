package vn.demo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.UserModel;

/**
 * REPOSITORY — collection {@code users}.
 *
 * <p>Dùng bởi seeder và {@code MongoUserDetailsService} khi login.</p>
 */
public interface UserRepository extends MongoRepository<UserModel, String> {

	Optional<UserModel> findByUsername(String username);

}
