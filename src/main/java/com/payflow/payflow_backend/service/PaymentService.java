package com.payflow.payflow_backend.service;

import com.payflow.payflow_backend.dto.request.PaymentRequest;
import com.payflow.payflow_backend.dto.response.PaymentResponse;
import com.payflow.payflow_backend.entity.Payment;
import com.payflow.payflow_backend.entity.Transaction;
import com.payflow.payflow_backend.entity.User;
import com.payflow.payflow_backend.enums.PaymentStatus;
import com.payflow.payflow_backend.enums.TransactionType;
import com.payflow.payflow_backend.exception.BadRequestException;
import com.payflow.payflow_backend.exception.ResourceNotFoundException;
import com.payflow.payflow_backend.repository.PaymentRepository;
import com.payflow.payflow_backend.repository.TransactionRepository;
import com.payflow.payflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final EmailService emailService;

    @Transactional
    public PaymentResponse createPayment(String senderEmail, PaymentRequest request) {
        if (senderEmail.equals(request.getReceiverEmail())) {
            throw new BadRequestException("Vous ne pouvez pas vous envoyer de l'argent");
        }

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Expéditeur non trouvé"));

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Destinataire non trouvé : "
                        + request.getReceiverEmail()));

        // Débiter l'expéditeur
        walletService.debit(sender, request.getAmount());

        // Créer le paiement
        Payment payment = Payment.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .description(request.getDescription())
                .reference(UUID.randomUUID().toString())
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // Simuler le prestataire de paiement → SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // Créditer le destinataire
        walletService.credit(receiver, request.getAmount());

        // Enregistrer les transactions
        com.payflow.payflow_backend.entity.Wallet senderWallet =
                walletService.getWalletEntity(sender);
        com.payflow.payflow_backend.entity.Wallet receiverWallet =
                walletService.getWalletEntity(receiver);

        transactionRepository.save(Transaction.builder()
                .wallet(senderWallet)
                .payment(payment)
                .type(TransactionType.DEBIT)
                .amount(request.getAmount())
                .build());

        transactionRepository.save(Transaction.builder()
                .wallet(receiverWallet)
                .payment(payment)
                .type(TransactionType.CREDIT)
                .amount(request.getAmount())
                .build());

        // Envoi des notifications par e-mail
        emailService.sendEmail(
                sender.getEmail(),
                "Paiement envoyé avec succès",
                "Vous avez envoyé " + request.getAmount() + " MRU à " + receiver.getFullName() + " (" + receiver.getEmail() + ")."
        );

        emailService.sendEmail(
                receiver.getEmail(),
                "Vous avez reçu un paiement !",
                "Vous avez reçu " + request.getAmount() + " MRU de la part de " + sender.getFullName() + " (" + sender.getEmail() + ")."
        );

        return toResponse(payment);
    }

    public List<PaymentResponse> getMyPayments(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return paymentRepository
                .findBySenderOrReceiverOrderByCreatedAtDesc(user, user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentByReference(String reference) {
        Payment payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement non trouvé : " + reference));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
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
}
