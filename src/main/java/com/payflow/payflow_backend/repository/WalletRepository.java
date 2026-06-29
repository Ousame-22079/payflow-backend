package com.payflow.payflow_backend.repository;

import com.payflow.payflow_backend.entity.Wallet;
import com.payflow.payflow_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
}
