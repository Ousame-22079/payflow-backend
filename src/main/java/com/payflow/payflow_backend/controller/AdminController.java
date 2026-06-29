package com.payflow.payflow_backend.controller;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.payflow.payflow_backend.dto.response.ApiResponse;
import com.payflow.payflow_backend.dto.response.DashboardResponse;
import com.payflow.payflow_backend.dto.response.PaymentResponse;
import com.payflow.payflow_backend.dto.response.UserResponse;
import com.payflow.payflow_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Tableau de bord", dashboard));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Liste des utilisateurs", users));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        List<PaymentResponse> payments = adminService.getAllPayments();
        return ResponseEntity.ok(
                ApiResponse.success("Liste des paiements", payments));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long id) {
        UserResponse user = adminService.toggleUserStatus(id);
        return ResponseEntity.ok(
                ApiResponse.success("Statut modifié", user));
    }
    @PostMapping("/users/{id}/recharge")
public ResponseEntity<ApiResponse<UserResponse>> rechargeWallet(
        @PathVariable Long id,
        @RequestBody Map<String, BigDecimal> body) {
    UserResponse user = adminService.rechargeWallet(id, body.get("amount"));
    return ResponseEntity.ok(ApiResponse.success("Wallet rechargé", user));
}
}
