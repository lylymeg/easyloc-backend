package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Avis_Evaluation")
@Data
@NoArgsConstructor
public class AvisEvaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Integer idAvis;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column
    private Integer note; 

    @Column(name = "date_creation")
    private LocalDate dateCreation = LocalDate.now();

  

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "id_client", nullable = false)
@JsonIgnoreProperties({"reservations", "avis", "password", "permisDoc"}) 
private Client client;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "id_vehicule", nullable = false)
@JsonIgnoreProperties({"reservations", "avis"}) 
private Vehicule vehicule;
}
