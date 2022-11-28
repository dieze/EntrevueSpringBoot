package com.example.entrevueSpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequiredArgsConstructor
public class FilmController {

    private final FilmRepository repository;

    /**
     * @see NullAsNotFoundResponseBodyAdvice about returning null
     */
    @GetMapping("/api/film/{id}")
    public Film getFilm(@PathVariable long id) { // @PathVariable required, long never null
        // two steps for debugging purposes
        //noinspection UnnecessaryLocalVariable
        Film film = repository.findById(id).orElse(null);
        return film;
    }

    @PostMapping("/api/film")
    @ResponseStatus(HttpStatus.CREATED)
    public Film postFilm(@Valid @RequestBody Film film) {
        // two steps for debugging purposes
        //noinspection UnnecessaryLocalVariable
        Film saved = repository.save(film);
        return saved;
    }
}
