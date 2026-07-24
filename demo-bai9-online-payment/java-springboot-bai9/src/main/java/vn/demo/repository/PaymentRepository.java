package vn.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.PaymentModel;

public interface PaymentRepository extends MongoRepository<PaymentModel, String> {

	Optional<PaymentModel> findByPaypalOrderId(String paypalOrderId);

	Optional<PaymentModel> findByCaptureId(String captureId);

	List<PaymentModel> findByStatusOrderByCreatedAtDesc(String status);
}
