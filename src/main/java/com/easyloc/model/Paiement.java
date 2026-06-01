package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Paiement")
@Data
@NoArgsConstructor
public class Paiement {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Integer idPaiement;

    @Column(nullable = false)
    private Float montant;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement = LocalDateTime.now();

    @Column(length = 50)
    private String methode;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement")
    private Statut statutPaiement = Statut.PAYE;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reservation", nullable = false)
    @JsonIgnoreProperties({"paiement", "contrat"}) 
    private Reservation reservation;

 
    public enum Statut { PAYE, ECHOUE }
}