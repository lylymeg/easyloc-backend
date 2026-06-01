package com.easyloc.dao;

import com.easyloc.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {
    Optional<Paiement> findByReservationIdReservation(Integer reservationId);
}