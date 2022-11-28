package com.example.entrevueSpringBoot;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity(name = "acteur")
// TODO: is acteur's combination of nom & prenom unique ?
// this is not specified on the instructions but
// some code (integration tests...) assumes it is
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"nom", "prenom"}))
@Getter
@Setter
public class FilmActeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // no @NotNull : IDENTITY can be null on insert
    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;
}
