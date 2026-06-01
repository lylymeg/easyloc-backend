package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@Table(name = "Vehicule")
@Data
@NoArgsConstructor
public class Vehicule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicule")
    
    private Integer idVehicule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 255)
    private String image;

    @Column(name = "nb_sieges")
    
    private Integer nbSieges;

    @Column(length = 30)
    private String couleur;

    @Column(name = "prix_journalier", nullable = false)
   
    private Float prixJournalier;

    @Column(length = 50)
    private String transmission;

    @Column(length = 50)
    private String carburant;

    @Column(name = "plaque_immat", unique = true, nullable = false, length = 20)
   
    private String plaqueImmat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.DISPONIBLE;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer kilometrage = 0;

    @ManyToOne
    @JoinColumn(name = "id_marque")
    @JsonIgnoreProperties("vehicules")
    private Marque marque;

    @OneToMany(mappedBy = "vehicule", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reservation> reservations;
// Dans Vehicule.java

@OneToMany(mappedBy = "vehicule", fetch = FetchType.LAZY)
@JsonIgnore 
private List<AvisEvaluation> avis;

    public enum Statut { 
        DISPONIBLE, 
        LOUE,
        EN_PANNE, 
        MAINTENANCE 
    }

    @Transient
    public boolean isReservable() {
        return statut == Statut.DISPONIBLE;
    }
}