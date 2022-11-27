package com.example.entrevueSpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FilmController {

    private final FilmRepository repository;

    @GetMapping("/api/film/{id}")
    public Film getFilm(@PathVariable long id) { // @PathVariable required, long never null
        // two steps for debugging purposes
        //noinspection UnnecessaryLocalVariable
        Film film = repository.getById(id);
        return film;
    }

    @PostMapping("/api/film")
    @ResponseStatus(HttpStatus.CREATED)
    public Film postFilm(@RequestBody Film film) {
        // two steps for debugging purposes
        //noinspection UnnecessaryLocalVariable
        Film saved = repository.save(film);
        return saved;
    }
}
