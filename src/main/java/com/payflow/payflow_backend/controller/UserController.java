package com.payflow.payflow_backend.controller;

import com.payflow.payflow_backend.dto.response.ApiResponse;
import com.payflow.payflow_backend.dto.response.UserResponse;
import com.payflow.payflow_backend.dto.response.WalletResponse;
import com.payflow.payflow_backend.entity.User;
import com.payflow.payflow_backend.entity.Wallet;
import com.payflow.payflow_backend.exception.BadRequestException;
import com.payflow.payflow_backend.exception.ResourceNotFoundException;
import com.payflow.payflow_backend.repository.UserRepository;
import com.payflow.payflow_backend.repository.WalletRepository;
import com.payflow.payflow_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        BigDecimal balance = walletRepository.findByUser(user)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        return ResponseEntity.ok(ApiResponse.success("Mon profil", buildResponse(user, balance)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (body.containsKey("fullName") && !body.get("fullName").isBlank()) {
            user.setFullName(body.get("fullName"));
        }
        if (body.containsKey("phone") && !body.get("phone").isBlank()) {
            user.setPhone(body.get("phone"));
        }
        userRepository.save(user);

        BigDecimal balance = walletRepository.findByUser(user)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour", buildResponse(user, balance)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            throw new BadRequestException("Champs manquants");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Mot de passe actuel incorrect");
        }

        if (newPassword.length() < 6) {
            throw new BadRequestException("Le mot de passe doit avoir au moins 6 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié", "OK"));
    }

    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
            @AuthenticationPrincipal UserDetails userDetails) {
        WalletResponse wallet = walletService.getMyWallet(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Mon wallet", wallet));
    }

    private UserResponse buildResponse(User user, BigDecimal balance) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .balance(balance)
                .build();
    }
}