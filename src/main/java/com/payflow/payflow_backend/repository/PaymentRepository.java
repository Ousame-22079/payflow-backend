package com.payflow.payflow_backend.repository;

import com.payflow.payflow_backend.entity.Payment;
import com.payflow.payflow_backend.entity.User;
import com.payflow.payflow_backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findBySenderOrReceiverOrderByCreatedAtDesc(
            User sender, User receiver);

    Optional<Payment> findByReference(String reference);

    long countByStatus(PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
}
