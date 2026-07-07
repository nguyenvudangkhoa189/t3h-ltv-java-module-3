package vn.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

import lombok.extern.slf4j.Slf4j;

/**
 * Tạo text index cho {@code $text} search (syllabus §4.3).
 *
 * <p>Mỗi collection chỉ có một text index — gộp {@code name} + {@code cuisine}.</p>
 * <p>{@code ensureIndex} an toàn khi index đã tồn tại (không tạo trùng).</p>
 */
@Slf4j
@Configuration
public class IndexConfig {

	@Bean
	CommandLineRunner ensureTextIndex(MongoTemplate mongoTemplate) {
		return args -> {
			TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
					.onField("name")
					.onField("cuisine")
					.build();
			mongoTemplate.indexOps("restaurants").ensureIndex(textIndex);
			log.info("Đã đảm bảo text index trên restaurants (name, cuisine)");
		};
	}

}
