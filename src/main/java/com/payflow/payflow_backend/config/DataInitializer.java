package com.payflow.payflow_backend.config;

import com.payflow.payflow_backend.entity.User;
import com.payflow.payflow_backend.entity.Wallet;
import com.payflow.payflow_backend.enums.Role;
import com.payflow.payflow_backend.repository.UserRepository;
import com.payflow.payflow_backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@payflow.com")) {
            User admin = User.builder()
                    .fullName("Super Admin")
                    .email("admin@payflow.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("22200000000")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);

            Wallet wallet = Wallet.builder()
                    .user(admin)
                    .build();
            walletRepository.save(wallet);

            System.out.println("✅ Admin créé : admin@payflow.com / admin123");
        }
    }
}
