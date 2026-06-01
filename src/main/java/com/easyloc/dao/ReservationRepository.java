package com.easyloc.dao;

import com.easyloc.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByClientIdUserOrderByDateReservationDesc(Integer clientId);
    List<Reservation> findByStatutReservation(Reservation.Statut statut);
    List<Reservation> findByStatutReservationOrderByDateDebutAsc(Reservation.Statut statut);
    long countByStatutReservation(Reservation.Statut statut);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.statutReservation = 'CONFIRMEE'
          AND r.dateFin < :now
    """)
    List<Reservation> findEnRetardNonBasculees(@Param("now") LocalDateTime now);

@Query("""
        SELECT r FROM Reservation r
        WHERE r.vehicule.idVehicule = :vehiculeId
          AND r.statutReservation = 'EN_ATTENTE'
          AND r.idReservation <> :currentResId
          AND NOT (r.dateFin <= :debut OR r.dateDebut >= :fin)
    """)
    List<Reservation> findConflits(
        @Param("vehiculeId") Integer vehiculeId,
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin,
        @Param("currentResId") Integer currentResId
    );
}