package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.persistence.DataGeneratorBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"test", "datagen"})
@SpringBootTest
public class OwnerServiceTest {

  @Autowired
  OwnerService ownerService;

  @Autowired
  DataGeneratorBean dataGeneratorBean;

  @BeforeEach
  public void setUp() throws SQLException {
    dataGeneratorBean.deleteData();
    dataGeneratorBean.generateData();
  }

  @Test
  public void shouldThrowValidationExceptionWhenInvalidOwnerIsCreated() {
    // given the current state of db
    // when
    OwnerCreateDto owner = new OwnerCreateDto(null, null, "invalid@testcom");
    ValidationException thrown = assertThrows(ValidationException.class, () -> {
      ownerService.create(owner);
    });

    // then
    assertAll(
            () -> assertEquals(3, thrown.errors().size()),
            () -> assertTrue(thrown.errors().contains("First name of the owner cannot be null")),
            () -> assertTrue(thrown.errors().contains("Last name of the owner cannot be null")),
            () -> assertTrue(thrown.errors().contains("Email of the owner is invalid"))
    );
  }

  @Test
  public void shouldThrowConflictExceptionWhenEmailIsNotUnique() {
    // given the current state of db
    // when
    OwnerCreateDto owner = new OwnerCreateDto("Owner", "Test", "owner@test.com");

    // then
    ConflictException thrown = assertThrows(ConflictException.class, () -> ownerService.create(owner));
    assertThat(thrown.getMessage()).contains("Email of the owner must be unique");
  }

}
