package com.example.entrevueSpringBoot;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RepositoryDefinition(domainClass = Film.class, idClass = Long.class)
public interface FilmTestRepository {

    Optional<Film> findByTitre(String name); // TODO: is film's titre unique ?

    /**
     * @see CrudRepository#deleteById
     */
    void deleteById(Long id);
}
