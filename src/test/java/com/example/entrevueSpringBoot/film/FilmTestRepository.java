package com.example.entrevueSpringBoot.film;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryDefinition(domainClass = Film.class, idClass = Long.class)
public interface FilmTestRepository {

    Film getByTitre(String name); // TODO: is film's titre unique ?

    /**
     * @see CrudRepository#deleteById
     */
    void deleteById(Long id);
}
