package com.example.entrevueSpringBoot.film;

import com.example.entrevueSpringBoot.acteur.Acteur;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.EAGER;

@Entity
// TODO: is film's titre unique ?
// this is not specified on the instructions but
// some code (integration tests...) assumes it is
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "titre"))
@Getter
@Setter
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;

    // TODO: can an acteur play in many films ?
    @ManyToMany(cascade = {
            PERSIST,MERGE,REFRESH,DETACH,
            //REMOVE // TODO: do we want to remove an acteur when removing a film ?
            // nothing is specified on the instructions!
            // we decided on a many-to-many relationship between film and acteur, i.e.
            // a film has many acteurs, an acteur can play on many films
            // so it does not make sense to remove an acteur when a film is removed...
    },
            // there is only one endpoint in the app
            // and it returns a film & its acteurs
            // EAGER is fine and will save round-trips to the database
            //
            // the only endpoints in this app are managing Film and Acteur
            // with the exact same fields as the entities so this app is using
            // the ORM entities as dtos on the endpoints for simplicity and conciseness
            // (avoiding dto classes and mappers that are useless for this very specific simple app)
            //
            // to prevent the View (JSON serialization process) modifying the underlying database,
            // we set property "spring.jpa.open-in-view=false" ; EAGER is handy because it prevents a
            // "org.hibernate.LazyInitializationException : could not initialize proxy – no Session"
            // when jackson calls Film#getActeurs()
            //
            // if we used fetch = LAZY we would need to call #getActeurs beforehand (purposely or
            // by mapping to a dto) in order to avoid the LazyInitializationException
            fetch = EAGER
    )
    @JoinTable(// TODO: can an acteur play in many films ?
            name = "acteurs_films", // convention: alphanumeric ("acteurs" first, then "films") & plural
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "acteur_id")
    )
    private List<Acteur> acteurs;
}
