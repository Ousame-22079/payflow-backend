package com.payflow.payflow_backend.controller;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.payflow.payflow_backend.dto.request.LoginRequest;
import com.payflow.payflow_backend.dto.request.RegisterRequest;
import com.payflow.payflow_backend.dto.response.ApiResponse;
import com.payflow.payflow_backend.dto.response.AuthResponse;
import com.payflow.payflow_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
// @SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Inscription réussie", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", response));
    }
}
