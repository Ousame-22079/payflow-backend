package com.payflow.payflow_backend.service;

import com.payflow.payflow_backend.dto.response.DashboardResponse;
import com.payflow.payflow_backend.dto.response.PaymentResponse;
import com.payflow.payflow_backend.dto.response.UserResponse;
import com.payflow.payflow_backend.entity.User;
import com.payflow.payflow_backend.entity.Wallet;
import com.payflow.payflow_backend.enums.PaymentStatus;
import com.payflow.payflow_backend.exception.ResourceNotFoundException;
import com.payflow.payflow_backend.repository.PaymentRepository;
import com.payflow.payflow_backend.repository.UserRepository;
import com.payflow.payflow_backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;

    public DashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalPayments = paymentRepository.count();

        long successPayments = paymentRepository.countByStatus(PaymentStatus.SUCCESS);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);

        BigDecimal totalVolume = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
        if (totalVolume == null) totalVolume = BigDecimal.ZERO;

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalPayments(totalPayments)
                .successPayments(successPayments)
                .failedPayments(failedPayments)
                .pendingPayments(pendingPayments)
                .totalVolume(totalVolume)
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé : " + userId));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        BigDecimal balance = walletRepository.findByUser(user)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

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

    private PaymentResponse toPaymentResponse(
            com.payflow.payflow_backend.entity.Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .senderEmail(payment.getSender().getEmail())
                .receiverEmail(payment.getReceiver().getEmail())
                .amount(payment.getAmount())
                .description(payment.getDescription())
                .status(payment.getStatus())
                .reference(payment.getReference())
                .createdAt(payment.getCreatedAt())
                .build();
    }
    @Transactional
public UserResponse rechargeWallet(Long userId, BigDecimal amount) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    walletService.credit(user, amount);
    return toUserResponse(user);
}
}
