
package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "Client")
@PrimaryKeyJoinColumn(name = "id_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Client extends Utilisateur {

  
    @Column(name = "permis_doc", columnDefinition = "LONGTEXT")
    private String permisDoc; 

    @Column(name = "num_permis", length = 50)
    private String numPermis;

    @Column(name = "date_naissance")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dateNaissance;

    @Column(name = "licence_date")
    @JsonFormat(pattern="yyyy-MM-dd") 
    private LocalDate licenceDate;

    @Column(name = "licence_expiry")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate licenceExpiry;

    @Column(name = "nb_locations_terminees")
    private Integer nbLocationsTerminees = 0;

  
    @Transient
    public boolean hasPermisValide() {
        if (licenceExpiry == null) return false;
        return licenceExpiry.isAfter(LocalDate.now());
    }


    @Transient
    public boolean hasAgeRequis(int ageMin) {
        if (dateNaissance == null) return false;
        return Period.between(dateNaissance, LocalDate.now()).getYears() >= ageMin;
    }

    @Transient
    public boolean isClientFidele() {
        return nbLocationsTerminees != null && nbLocationsTerminees >= 3;
    }

    @Transient
    @JsonProperty("tauxRemise")
    public float getTauxRemise() {
        return isClientFidele() ? 0.10f : 0.0f;
    }

      @Transient
    public float appliquerRemise(float montantBase) {
        return montantBase * (1f - getTauxRemise());
    }
}