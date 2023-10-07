package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.persistence.DataGeneratorBean;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest {

  @Autowired
  HorseService horseService;

  @Autowired
  DataGeneratorBean dataGeneratorBean;

  @BeforeEach
  public void setup() throws SQLException {
    dataGeneratorBean.deleteData();
    dataGeneratorBean.generateData();
  }

  @Test
  public void getAllReturnsAllStoredHorses() {
    // given the current state of db
    // when
    List<HorseListDto> horses = horseService.allHorses()
            .toList();

    // then
    assertThat(horses.size()).isGreaterThanOrEqualTo(1);
    assertThat(horses)
            .map(HorseListDto::id, HorseListDto::name)
            .contains(tuple(-1L, "Wendy"), tuple(-2L, "Baba"),
                    tuple(-3L, "Mendy"), tuple(-4L, "Bella"),
                    tuple(-5L, "JJJ"), tuple(-6L, "George"),
                    tuple(-7L, "Bullseye"), tuple(-8L, "Plane"),
                    tuple(-9L, "Pegasus"), tuple(-10L, "THY"));
  }

  @Test
  public void shouldSuccessfullyCreateHorse() throws ValidationException, ConflictException {
    // given the current state of db
    // when
    HorseDetailDto response = horseService.create(new HorseCreateDto("Horse", "Service Test create",
            LocalDate.of(1999, 10, 10), Sex.MALE, null, null, null));

    // then
    assertAll(
            () -> assertThat(response.id()).isNotNull(),
            () -> assertThat(response.name()).isEqualTo("Horse"),
            () -> assertThat(response.description()).isEqualTo("Service Test create"),
            () -> assertThat(response.dateOfBirth()).isEqualTo("1999-10-10"),
            () -> assertThat(response.sex()).isEqualTo(Sex.MALE)
    );
  }

  @Test
  @DisplayName("should throw validation exception when a horse is created "
          + "with name null, empty description and date of birth in the future")
  public void shouldThrowValidationExceptionWithWrongValues() {
    // given the current state of db
    // when
    HorseCreateDto horseToCreate = new HorseCreateDto(
                null, "", LocalDate.now().plusDays(10),
            Sex.MALE, null, null, null);

    // then
    ValidationException thrown = assertThrows(ValidationException.class, () -> {
      horseService.create(horseToCreate);
    });

    assertAll(
            () -> assertEquals(3, thrown.errors().size()),
            () -> assertTrue(thrown.errors().contains("Name of the horse cannot be null")),
            () -> assertTrue(thrown.errors().contains("Horse description is given but blank")),
            () -> assertTrue(thrown.errors().contains("Date of birth of the horse cannot be in the future"))
    );
  }

  @Test
  public void shouldThrowConflictExceptionWhenChangingParentsSexAndDateOfBirth() {
    // given the current state of db
    // when
    HorseDetailDto horse = new HorseDetailDto(-1L, "Wendy", "Grand Mother",
            LocalDate.parse("2015-12-12"), Sex.MALE, null, null, null);

    // then
    ConflictException thrown = assertThrows(ConflictException.class, () -> {
      horseService.update(horse);
    });

    assertAll(
            () -> assertEquals(2, thrown.errors().size()),
            () -> assertTrue(thrown.errors().contains("The sex of the horse cannot be changed, as it has children")),
            () -> assertTrue(thrown.errors().contains("The date of birth of the horse cannot be changed, as it has children"))
    );
  }
}
