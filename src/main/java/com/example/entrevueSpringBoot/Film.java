package com.example.entrevueSpringBoot;

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

    @OneToMany(cascade = {
            // ALL:
            PERSIST,MERGE,REFRESH,DETACH,
            REMOVE
            // TODO: do we want to remove an acteur when removing a film ?
            // there is no endpoint for managing acteurs and creating
            // an acteur is through POST film so it seems like, yes
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
    // TODO: can an acteur only play in one film ?
    // nothing is specified on the instructions!
    //
    // There is no dedicated endpoints for managing acteurs
    // acteurs are created on POST film so "an acteur can only play in one film"
    // is simpler because on POST film we don't have to figure-out if the acteurs
    // already exist or not
    //
    // -> acteur table has foreign key "film_id"
    @JoinColumn(name="film_id")
    private List<FilmActeur> acteurs;
}
