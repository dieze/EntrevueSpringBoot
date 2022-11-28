package com.example.entrevueSpringBoot.acteur;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "acteur")
// TODO: is acteur's combination of nom & prenom unique ?
// this is not specified on the instructions but
// some code (integration tests...) assumes it is
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"nom", "prenom"}))
@Getter
@Setter
public class Acteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
}
