package com.example.entrevueSpringBoot;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary // conflicting FilmRepository
public interface FilmTestRepository extends FilmRepository {

    Optional<Film> findByTitre(String name); // TODO: is film's titre unique ?

    /**
     * @see CrudRepository#deleteById
     */
    void deleteById(Long id);
}
