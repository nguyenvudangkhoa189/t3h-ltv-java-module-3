package vn.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Cấu hình MongoDB Transaction (§8.2 syllabus).
 *
 * <p>Bean {@link MongoTransactionManager} bắt buộc để {@code @Transactional} có hiệu lực
 * với MongoDB. Nếu thiếu bean này, annotation bị bỏ qua âm thầm (silent no-op).</p>
 *
 * <p>Yêu cầu: MongoDB chạy ở chế độ <b>Replica Set</b> (không phải standalone).</p>
 */
@Configuration
public class MongoConfig {

	@Bean
	MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
		return new MongoTransactionManager(dbFactory);
	}

}
