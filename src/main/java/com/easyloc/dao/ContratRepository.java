package com.easyloc.dao;

import com.easyloc.model.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Integer> {
    Optional<Contrat> findByReservationIdReservation(Integer reservationId);
}