package com.example.entrevueSpringBoot.acteur;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryDefinition(domainClass = Acteur.class, idClass = Long.class)
public interface ActeurRepository {

    List<Acteur> findAllByIdIn(List<Long> id);

    /**
     * @see CrudRepository#save
     */
    Acteur save(Acteur acteur);
}
