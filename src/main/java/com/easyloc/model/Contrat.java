package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Contrat")
@Data
@NoArgsConstructor
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Integer idContrat;

    
    @Column(columnDefinition = "LONGTEXT")
    private String contenu;

  
    @Column(name = "date_signature")
    private LocalDateTime dateSignature;

    
    @Column(name = "signature_image", columnDefinition = "LONGTEXT")
    private String signatureImage;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reservation", nullable = false)
    private Reservation reservation;

  
    @Transient
    public boolean isSigne() {
        return signatureImage != null && !signatureImage.isBlank();
    }
}
