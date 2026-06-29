package com.payflow.payflow_backend.controller;
import com.payflow.payflow_backend.dto.request.PaymentRequest;
import com.payflow.payflow_backend.dto.response.ApiResponse;
import com.payflow.payflow_backend.dto.response.PaymentResponse;
import com.payflow.payflow_backend.dto.response.WalletResponse;
import com.payflow.payflow_backend.service.PaymentService;
import com.payflow.payflow_backend.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(
                userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Paiement effectué", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PaymentResponse> payments = paymentService.getMyPayments(
                userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Historique des paiements", payments));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByReference(
            @PathVariable String reference) {
        PaymentResponse payment = paymentService.getPaymentByReference(reference);
        return ResponseEntity.ok(ApiResponse.success("Paiement trouvé", payment));
    }

    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
            @AuthenticationPrincipal UserDetails userDetails) {
        WalletResponse wallet = walletService.getMyWallet(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Mon wallet", wallet));
    }
}
