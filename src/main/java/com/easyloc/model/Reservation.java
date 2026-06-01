package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@Entity
@Table(name = "Reservation")
@Data
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation")
    private Integer idReservation;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Column(name = "date_reservation")
    private LocalDateTime dateReservation = LocalDateTime.now();

   
     
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_reservation", nullable = false)
    private Statut statutReservation = Statut.EN_ATTENTE;

    @Column(name = "montant_total")
    private Float montantTotal;

  
    @Column(name = "montant_penalite")
    private Float montantPenalite = 0f;



@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "id_client", nullable = false)
@JsonIgnoreProperties({"reservations", "avis", "password"})
private Client client;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "id_vehicule", nullable = false)
@JsonIgnoreProperties({"reservations", "avis"})
private Vehicule vehicule;

@OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
@JsonIgnoreProperties("reservation") 
private Paiement paiement;

@OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
@JsonIgnoreProperties("reservation") 
private Contrat contrat;

@Column(name = "jours_retard")
private Integer joursRetard;



    public enum Statut {
        CONFIRMEE, EN_ATTENTE, TERMINEE, ANNULEE, RETARD
    }

    @Transient
    public long getNbJours() {
       
        if (dateDebut == null || dateFin == null) return 1;
        try {
            return Math.max(1, ChronoUnit.DAYS.between(dateDebut, dateFin));
        } catch (Exception e) {
            return 1;
        }
    }

    @Transient
    public float calculerMontantBase() {
        
        if (vehicule == null) return 0f;
        return vehicule.getPrixJournalier() * getNbJours();
    }

    @Transient
    public float calculerPenalite() {
      
        if (vehicule == null || dateFin == null) return 0f;
        
        long jours = 0;
        try {
            jours = Math.max(0, ChronoUnit.DAYS.between(dateFin, LocalDateTime.now()));
        } catch (Exception e) {
            return 0f;
        }
        
        if (jours <= 0) return 0f;
        return vehicule.getPrixJournalier() * 1.5f * jours;
    }

    @Transient
    public float getMontantFinal() {
       
        float total = (montantTotal != null) ? montantTotal : 0f;
        float penalite = (montantPenalite != null) ? montantPenalite : 0f;
        return total + penalite;
    }
  
   
public Integer getJoursRetard() {
    return joursRetard;
}

public void setJoursRetard(Integer joursRetard) {
    this.joursRetard = joursRetard;
}

   

    
    @Transient
    public boolean isActive() {
        return statutReservation == Statut.CONFIRMEE
            || statutReservation == Statut.EN_ATTENTE
            || statutReservation == Statut.RETARD;
    }
}




