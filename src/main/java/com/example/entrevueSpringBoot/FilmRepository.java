package com.example.entrevueSpringBoot;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RepositoryDefinition(domainClass = Film.class, idClass = Long.class)
public interface FilmRepository {

    /**
     * @see CrudRepository#save
     */
    Film save(Film entity);

    /**
     * @see CrudRepository#findById
     */
    Optional<Film> findById(long id);
}
