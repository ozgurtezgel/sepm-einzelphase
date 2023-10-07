package at.ac.tuwien.sepm.assignment.individual.rest;

import java.util.List;

/**
 * DTO to encapsulate conflict errors.
 * Contains a message and a list of errors
 */
public record ConflictErrorRestDto(
        String message,
        List<String> errors
) {
}

