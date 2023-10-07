package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * DTO to encapsulate the family tree
 * Contains id, name, dateOfBirth, sex
 * and mother and father as {@link HorseFamilyTreeDto}
 */
public record HorseFamilyTreeDto(
        Long id,
        String name,
        LocalDate dateOfBirth,
        Sex sex,
        HorseFamilyTreeDto mother,
        HorseFamilyTreeDto father
) {

}

