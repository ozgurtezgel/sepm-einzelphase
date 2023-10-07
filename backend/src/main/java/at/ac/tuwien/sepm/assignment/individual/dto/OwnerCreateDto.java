package at.ac.tuwien.sepm.assignment.individual.dto;

/**
 * DTO to encapsulate parameters for Owner create.
 * Contains all common properties
 */
public record OwnerCreateDto(
    String firstName,
    String lastName,
    String email
) {
}
