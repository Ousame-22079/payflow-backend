package com.payflow.payflow_backend.service;

import com.payflow.payflow_backend.dto.response.WalletResponse;
import com.payflow.payflow_backend.entity.User;
import com.payflow.payflow_backend.entity.Wallet;
import com.payflow.payflow_backend.exception.ResourceNotFoundException;
import com.payflow.payflow_backend.repository.UserRepository;
import com.payflow.payflow_backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletResponse getMyWallet(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet non trouvé"));
        return toResponse(wallet);
    }

    @Transactional
    public void credit(User user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet non trouvé"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void debit(User user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet non trouvé"));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new com.payflow.payflow_backend.exception.BadRequestException("Solde insuffisant");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    private WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .ownerEmail(wallet.getUser().getEmail())
                .balance(wallet.getBalance())
                .build();
    }
    public Wallet getWalletEntity(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet non trouvé"));
    }
}
