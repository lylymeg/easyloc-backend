package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Table mère de l'héritage.
 * Stratégie JOINED : chaque sous-classe a sa propre table
 * (Client et Admin ont leur table avec FK vers Utilisateur)
 */
@Entity
@Table(name = "Utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public class Utilisateur {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
private String telephone; 

@Column(columnDefinition = "LONGTEXT") 
private String photoUrl; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role { ADMIN, CLIENT }
}
