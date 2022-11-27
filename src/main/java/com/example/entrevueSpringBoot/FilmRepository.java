package com.example.entrevueSpringBoot;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Optional;

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
public class FilmRepository {

    @Autowired // caveat: no setter injection possible. only FilmRepository knows FilmJpaRepository
    @Lazy // required FilmRepository both declares @Bean JpaRepositoryFactoryBean<FilmJpaRepository> and uses it
    @Delegate(excludes = Excludes.class)
    private FilmJpaRepository jpaRepository;

    // lombok @Delegate excludes
    private interface Excludes {
        Optional<Film> findById(long id); // use decorated FilmRepository#getById(long) instead
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

    private interface FilmJpaRepository extends org.springframework.data.repository.Repository<Film, Long> {

        /**
         * @see CrudRepository#save
         */
        Film save(Film entity);

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
