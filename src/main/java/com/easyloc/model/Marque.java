package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity @Table(name = "Marque")
@Data @NoArgsConstructor
public class Marque {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marque")
    private Integer idMarque;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(length = 255)
    private String logo;


    
    @OneToMany(mappedBy = "marque", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("marque")
  

private List<Vehicule> vehicules;

}