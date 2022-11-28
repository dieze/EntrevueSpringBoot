package com.example.entrevueSpringBoot;

import org.hibernate.engine.jdbc.internal.ResultSetReturnImpl;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

/**
 * Controller advice for handling DAO exceptions.
 *
 * No custom logging, SQL-related exceptions should have been automatically logged already.
 *
 * @see SqlExceptionHelper#logExceptions
 * @see ResultSetReturnImpl#executeUpdate that logs SQLException for update method
 */
@ControllerAdvice
public class DaoExceptionsControllerAdvice {

    /**
     * @see Film unique constraint on titre
     * @see FilmActeur unique constraint on combination of nom & prenom
     * TODO: is film's titre unique ?
     * TODO: is acteur's combination of nom & prenom unique ?
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public String handle(DataIntegrityViolationException e) {
        // vendor-specific code
        ConstraintViolationException constraintException = (ConstraintViolationException) e.getCause(); // spring -> hibernate
        SQLException sqlException = constraintException.getSQLException(); // hibernate -> JDK

        if (!sqlException.getMessage().contains("Unique index or primary key violation")) {
            throw e; // sorry, can only process unique constraint violation
        }

        "Unique index or primary key violation: \"PUBLIC.UK4SS6BMOSO9FGRIIED36YKO3OY_INDEX_2 ON PUBLIC.FILM(TITRE) VALUES 1\"

        return ((ConstraintViolationException) e.getCause()).getSQLException().getMessage(); // useful message for devs
    }
}
