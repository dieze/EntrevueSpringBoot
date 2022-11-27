package com.example.entrevueSpringBoot;

import org.hibernate.engine.jdbc.internal.ResultSetReturnImpl;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice for handling DAO exceptions.
 *
 * No custom logging, SQL-related exceptions should have been automatically logged already.
 *
 * @see SqlExceptionHelper#logExceptions
 * @see ResultSetReturnImpl#executeUpdate that logs SQLException for update method
 */
@RestControllerAdvice
public class DaoExceptionsControllerAdvice {

    /**
     * @see Film unique constraint on titre
     * @see FilmActeur unique constraint on combination of nom & prenom
     * TODO: is film's titre unique ?
     * TODO: is acteur's combination of nom & prenom unique ?
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handle(DataIntegrityViolationException e) {
        //if (production environment)
        //return ""; // do not propagate security-sensible information to caller

        // spring -> hibernate ; hibernate specific code
        return ((ConstraintViolationException) e.getCause()).getSQLException().getMessage(); // useful message for devs
    }
}
