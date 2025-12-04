package org.example.microservicepayment.repository;

import org.example.microservicepayment.entity.Payment;
import org.example.microservicepayment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByReservationId(Long reservationId);

  List<Payment> findByStatus(PaymentStatus status);

  List<Payment> findByReservationIdAndStatus(Long reservationId, PaymentStatus status);
}
