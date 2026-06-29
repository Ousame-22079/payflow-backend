package com.payflow.payflow_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private long totalUsers;
    private long totalPayments;
    private long successPayments;
    private long failedPayments;
    private long pendingPayments;
    private BigDecimal totalVolume;
}
