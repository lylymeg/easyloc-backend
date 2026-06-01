package com.easyloc.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Admin")
@PrimaryKeyJoinColumn(name = "id_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Admin extends Utilisateur {
   
}
