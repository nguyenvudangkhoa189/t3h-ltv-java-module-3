package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DemoBai9OnlinePaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoBai9OnlinePaymentApplication.class, args);
	}
}
