package vn.demo.dto;

public record PaymentResultDto(String orderId, String status, String captureId) {
}
