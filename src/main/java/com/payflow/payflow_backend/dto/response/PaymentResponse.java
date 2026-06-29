package com.payflow.payflow_backend.dto.response;

import com.payflow.payflow_backend.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private String senderEmail;
    private String receiverEmail;
    private BigDecimal amount;
    private String description;
    private PaymentStatus status;
    private String reference;
    private LocalDateTime createdAt;
}
