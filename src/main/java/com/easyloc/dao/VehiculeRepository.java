package com.easyloc.dao;

import com.easyloc.model.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;



// src/main/java/com/easyloc/repository/VehiculeRepository.java

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Integer> {

    List<Vehicule> findByStatut(Vehicule.Statut statut);

    /**
     * Recherche avec filtres optionnels. 
     * Note : On utilise v.marque.idMarque pour filtrer par l'ID de l'objet lié.
     */
    @Query("""
        SELECT v FROM Vehicule v
        WHERE (:statut IS NULL OR v.statut = :statut)
          AND (:nom       IS NULL OR LOWER(v.nom)      LIKE LOWER(CONCAT(:nom,'%')))
          AND (:carburant IS NULL OR v.carburant         = :carburant)
          AND (:trans     IS NULL OR v.transmission      = :trans)
          AND (:prixMax   IS NULL OR v.prixJournalier   <= :prixMax)
          AND (:marqueId  IS NULL OR v.marque.idMarque   = :marqueId)
        ORDER BY v.prixJournalier ASC
    """)
    List<Vehicule> findDisponiblesAvecFiltres(
            @Param("nom")       String nom,
            @Param("carburant") String carburant,
            @Param("trans")     String trans,
            @Param("prixMax")   Float prixMax,
            @Param("marqueId")  Integer marqueId, // Renommé pour correspondre au Controller
            @Param("statut")    Vehicule.Statut statut
    );

    long countByStatut(Vehicule.Statut statut);

   
    @Query("""
        SELECT COUNT(r) = 0 FROM Reservation r
        WHERE r.vehicule.idVehicule = :vid
          AND r.statutReservation IN ('CONFIRMEE')
          AND NOT (r.dateFin <= :debut OR r.dateDebut >= :fin)
    """)
    boolean isDisponiblePeriode(
            @Param("vid")   Integer vehiculeId,
            @Param("debut") LocalDateTime debut,
            @Param("fin")   LocalDateTime fin
    );
}