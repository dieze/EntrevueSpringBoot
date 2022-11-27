package com.example.entrevueSpringBoot;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entity could not be found.
 */
@Getter
public class EntityNotFoundException extends RuntimeException {

    private final String entityName;
    private final Map<String, Object> findByCriteria; // "criteria" plural of "criterion"

    public EntityNotFoundException(String entityName, Map<String, Object> findByCriteria) {
        super(
                String.format(
                        "Could not find %s by %s",
                        entityName,
                        findByCriteria.entrySet().stream()
                                .map(e -> String.format("%s:%s", e.getKey(), e.getValue()))
                                .collect(Collectors.joining(","))
                ) // <- exception message
        );

        this.entityName = entityName;

        // exception happened for specific criteria
        // these should not be modified
        this.findByCriteria = Collections.unmodifiableMap(findByCriteria);
    }
}
