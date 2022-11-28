package com.example.entrevueSpringBoot.film;

import com.example.entrevueSpringBoot.EntityNotFoundException;
import com.example.entrevueSpringBoot.acteur.Acteur;
import com.example.entrevueSpringBoot.acteur.ActeurRepository;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for Film.
 *
 * This is not a JPA repository, but it delegates all its methods to a JPA repository, and decorates some methods.
 * The underlying JPA repository is private so its methods can not be accessed from outside of FilmRepository.
 * This has the benefit that we know for sure (100% sure) that no one is going to call the un-decorated methods
 * of FilmJpaRepository directly (except using reflection of course).
 *
 * A private JPA repository could not be possible using the conventional way, i.e. annotating the interface with
 * annnotation @Repository and letting component-scan finding it because component-scan ignores private nested members.
 *
 * This is made possible here because {@link #filmJpaRepositoryFactory()} manually declares the {@link JpaRepositoryFactoryBean}
 * for {@link FilmJpaRepository} but one caveat is that {@link JpaRepositoriesAutoConfiguration}
 * has @ConditionalOnMissingBean(JpaRepositoryFactoryBean.class) so we need to explicitly use @EnableJpaRepositories
 * to still have a configuration equivalent to the auto-configuration :
 *
 * JpaRepositoriesAutoConfiguration: "Once in effect, the auto-configuration is the equivalent of enabling
 * JPA repositories using the @EnableJpaRepositories annotation."
 */
@Repository
@RequiredArgsConstructor
@Transactional // required on #save so that ActeurRepository#find... and FilmJpaRepository#save are made on same transaction
public class FilmRepository {

    @Autowired // caveat: no setter injection possible. only FilmRepository knows FilmJpaRepository
    @Lazy // required FilmRepository both declares @Bean JpaRepositoryFactoryBean<FilmJpaRepository> and uses it
    @Delegate(excludes = Excludes.class) // keeping delegation for future new methods of FilmJpaRepository
    private FilmJpaRepository jpaRepository;

    private final JdbcTemplate jdbcTemplate;
    private final ActeurRepository acteurRepository;

    // lombok @Delegate excludes
    private interface Excludes {
        Optional<Film> findById(long id); // use decorated FilmRepository#getById(long) instead
        Film save(Film entity);
    }

    /**
     * @param id Film id, cannot be null
     */
    public Film getById(long id) throws EntityNotFoundException {
        return jpaRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(
                        "film", // entity name
                        Collections.singletonMap("id", id) // criteria
                )
        );
    }

    /**
     * Save a Film.
     *
     * When saving a new Film we must check if its {@link Film#getActeurs()} without id already exist in database
     * based on their nom & prenom otherwise saving would throw a {@link DataIntegrityViolationException} due to
     * UNIQUE constraint in database.
     *
     * TODO: is auteur's combination of nom & prenom unique ?
     */
    public Film save(Film film) {
        // when saving a new Film we must check if its acteurs without id already exist in database
        // based on the combination of their nom & prenom, otherwise saving would throw a DataIntegrityViolationException
        // due to UNIQUE constraint on nom & prenom in database
        //
        // in SQL this can be done in a single query as follow:
        // SELECT * FROM acteur WHERE (nom, prenom) IN (('Ford', 'Harrison'),('Hamill', 'Mark'));
        //
        // unfortunately Spring Data does not support tuples (I tried):
        // https://github.com/spring-projects/spring-data-jpa/issues/1609#issuecomment-752460750 Multiple Columns In Clause Parameters
        //
        // one alternative/hack is to use CONCAT:
        // https://stackoverflow.com/q/37583109/#62910678 Multiple column IN clause spring data jpa
        //
        // a better approach is to go programmatic and use JDBCTemplate:
        // https://stackoverflow.com/q/23305553 how to bind a list of tuples using Spring JDBCTemplate?

        if (film.getActeurs() != null) {

            // find film's acteurs sans id
            Map<NomPrenom, Integer> nomPrenomToActeurIdx = new HashMap<>();
            for (int i = 0; i < film.getActeurs().size(); i++) {
                Acteur acteur = film.getActeurs().get(i);
                if (acteur.getId() != null) continue;
                if (acteur.getNom() == null) continue; // invalid TODO: is acteur's nom required ?
                if (acteur.getPrenom() == null) continue; // invalid TODO: is acteur's prenom required ?

                nomPrenomToActeurIdx.put(new NomPrenom(acteur), i);
            }

            // search these acteurs by nom & prenom in db with optimized query
            // using JDBC because JPA can't handle IN tuples
            // TODO: [security] unsafe SQL statement
            List<Map<String, Object>> dbActeurs = jdbcTemplate.queryForList(String.format(
                    "SELECT * FROM acteur WHERE (nom, prenom) IN (%s)",
                    nomPrenomToActeurIdx.keySet().stream()
                            .map(np -> String.format("('%s','%s')", np.nom, np.prenom))
                            .collect(Collectors.joining(","))
            ));

            // search these same acteurs by id using JPA/Hibernate so these are in the persistence context
            // unfortunately JDBC can't (don't know how to) update the persistence context
            //
            // we can't set the id ourselves, it is managed by Hibernate and very sensitive
            // https://stackoverflow.com/q/13370221/#comment63586228_13370221 PersistentObjectException: detached entity passed to persist thrown by JPA and Hibernate
            List<Long> acteursIds = dbActeurs.stream().map(m -> (Long) m.get("id")).collect(Collectors.toList());
            List<Acteur> managedActeurs = acteurRepository.findAllByIdIn(acteursIds); // requires @Transactional

            // update film with the managed acteurs
            for (Acteur managedActeur : managedActeurs) {
                Integer acteurIdx = nomPrenomToActeurIdx.get(
                        new NomPrenom(managedActeur.getNom(), managedActeur.getPrenom()) // NomPrenom map key
                );
                film.getActeurs().set(acteurIdx, managedActeur);
            }
        }

        // finally, save the film with all managed acteurs
        return jpaRepository.save(film); // requires @Transactional
    }
    @RequiredArgsConstructor
    @EqualsAndHashCode // for #save, helper class as Map key thanks to lombok's generated #equals, #hashCode
    private class NomPrenom {
        private final String nom;
        private final String prenom;
        public NomPrenom(Acteur acteur) {
            nom = acteur.getNom();
            prenom = acteur.getPrenom();
        }
    }

    private interface FilmJpaRepository extends org.springframework.data.repository.Repository<Film, Long> {

        /**
         * @see CrudRepository#save
         */
        Film save(Film film);

        /**
         * @param id Film id, cannot be null
         *
         * @see CrudRepository#findById
         */
        // do not use JpaRepository#getById ! unless you do map to dto
        //
        // JpaRepository#getById does not hit the db,
        //   it returns a reference (proxy) with only the id,
        //   and the proxy lazily hit the database when requesting entity propeties
        //
        // CrudRepository#findById does hit the db
        //   it returns the actual data (no proxy) except relations with fetch = LAZY that query database on demand
        //
        // https://stackoverflow.com/q/69109649/#70293442 Hibernate: findById vs getbyId
        Optional<Film> findById(long id);
    }

    // Spring Configuration

    /**
     * Declare the JpaRepositoryFactoryBean for FilmJpaRepository.
     *
     * Having @Bean inside @Component makes it evaluated in "lite" mode.
     * But that's fine since there is no inter-@Bean dependencies.
     *
     * Note that this declaration of a JpaRepositoryFactoryBean bean disables JPA repositories auto-configuration,
     * see {@link JpaRepositoriesAutoConfiguration}'s @ConditionalOnMissingBean(JpaRepositoryFactoryBean.class)
     *
     * @see <a href="https://www.logicbig.com/tutorials/spring-framework/spring-core/bean-definition-in-components.html">@Bean definition in @Component</a>
     */
    @Bean
    public JpaRepositoryFactoryBean<FilmJpaRepository, Film, Long> filmJpaRepositoryFactory() {
        return new JpaRepositoryFactoryBean<>(FilmJpaRepository.class); // @Autowired will be processed
    }
}
