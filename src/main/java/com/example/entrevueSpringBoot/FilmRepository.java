package com.example.entrevueSpringBoot;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface FilmRepository extends Repository<Film, Long> {

    /**
     * @see CrudRepository#save
     */
    Film save(Film entity);

    /**
     * @see CrudRepository#findById
     */
    Optional<Film> findById(long id);
}
