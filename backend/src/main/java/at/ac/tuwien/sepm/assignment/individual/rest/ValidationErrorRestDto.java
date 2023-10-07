package at.ac.tuwien.sepm.assignment.individual.rest;

import java.util.List;

/**
 * DTO to encapsulate validation errors.
 * Contains a message and a list of errors
 */
public record ValidationErrorRestDto(
    String message,
    List<String> errors
) {
}
