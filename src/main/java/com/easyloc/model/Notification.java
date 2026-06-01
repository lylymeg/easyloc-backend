package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notif")
    private Integer idNotif;

    @Column(name = "titre", length = 100)
    private String titre;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @Column(name = "lu", nullable = false)
    private Boolean lu = false;

   
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_user", nullable = false)
    @JsonIgnoreProperties({"password", "photoUrl", "telephone", "role", "reservations", "avis"})
    private Utilisateur user;
}