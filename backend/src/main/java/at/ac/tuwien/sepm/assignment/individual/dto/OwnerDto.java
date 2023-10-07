package at.ac.tuwien.sepm.assignment.individual.dto;

/**
 * Class for Owner DTO
 * Contains all common properties
 */
public record OwnerDto(
    long id,
    String firstName,
    String lastName,
    String email
) {
}
