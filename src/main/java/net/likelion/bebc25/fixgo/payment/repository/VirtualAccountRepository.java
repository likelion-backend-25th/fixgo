package net.likelion.bebc25.fixgo.payment.repository;

import net.likelion.bebc25.fixgo.payment.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {
    Optional<VirtualAccount> findByPaymentAttemptId(Long attemptId);
}
