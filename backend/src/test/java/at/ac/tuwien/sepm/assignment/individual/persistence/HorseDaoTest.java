package at.ac.tuwien.sepm.assignment.individual.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;

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
    List<Horse> horses = horseDao.getAll();

    // then
    assertThat(horses.size()).isGreaterThanOrEqualTo(10);
    assertThat(horses)
        .extracting(Horse::getId, Horse::getName)
        .contains(tuple(-1L, "Wendy"), tuple(-2L, "Baba"),
                tuple(-3L, "Mendy"), tuple(-4L, "Bella"),
                tuple(-5L, "JJJ"), tuple(-6L, "George"),
                tuple(-7L, "Bullseye"), tuple(-8L, "Plane"),
                tuple(-9L, "Pegasus"), tuple(-10L, "THY"));
  }

  @Test
  public void shouldSuccessfullyCreateHorse() {
    // given the current state of db
    // when
    Horse horse = horseDao.create(
            new HorseCreateDto("Horse", "Dao Test Create",
                    LocalDate.parse("2000-02-03"), Sex.MALE, null, null, null));

    // then
    assertAll(
            () -> assertThat(horse.getId()).isNotNull(),
            () -> assertThat(horse.getName()).isEqualTo("Horse"),
            () -> assertThat(horse.getDescription()).isEqualTo("Dao Test Create"),
            () -> assertThat(horse.getDateOfBirth()).isEqualTo("2000-02-03"),
            () -> assertThat(horse.getSex()).isEqualTo(Sex.MALE),
            () -> assertThat(horse.getOwnerId()).isNull(),
            () -> assertThat(horse.getMotherId()).isNull(),
            () -> assertThat(horse.getFatherId()).isNull()
    );
  }

  @Test
  public void shouldSuccessfullyUpdateHorse() throws Exception {
    // given the current state of db
    // when
    Horse horse = horseDao.update(
            new HorseDetailDto(
                    -1L,
                    "Test Horse",
                    "it is already updated",
                    LocalDate.parse("1996-12-12"),
                    Sex.MALE,
                    null,
                    null,
                    null));

    // then
    assertAll(
            () -> assertThat(horse.getId()).isNotNull(),
            () -> assertThat(horse.getName()).isEqualTo("Test Horse"),
            () -> assertThat(horse.getDescription()).isEqualTo("it is already updated"),
            () -> assertThat(horse.getDateOfBirth()).isEqualTo("1996-12-12"),
            () -> assertThat(horse.getSex()).isEqualTo(Sex.MALE)
    );
  }


  @Test
  public void shouldSuccessfullyDeleteHorseAndUpdateChild() throws Exception {
    // given the current state of db
    // when
    horseDao.deleteById(-4L);

    // then
    NotFoundException thrown = assertThrows(NotFoundException.class, () -> horseDao.getById(-4));
    assertAll(
            () -> assertThat(thrown.getMessage()).contains("No horse with ID -4 found"),
            () -> assertThat(horseDao.getAll().size()).isEqualTo(9),
            () -> assertThat(horseDao.getById(-5).getMotherId()).isNull()
    );
  }

  @Test
  public void shouldSuccessfullySearchHorses() {
    // given the current state of db
    // when
    HorseSearchDto horseSearchDto = new HorseSearchDto(null, null, LocalDate.parse("2017-01-01"),
            Sex.FEMALE, null, 10);

    // then
    List<Horse> horses = horseDao.searchHorses(horseSearchDto);
    assertThat(horses.size()).isEqualTo(3);
  }

  @Test
  public void shouldThrowExceptionWhenUpdatingNonExistingHorse() {
    // given the current state of db
    HorseDetailDto horse = new HorseDetailDto(-999L, "Test Horse",
                    "it is already updated", LocalDate.parse("1996-12-12"),
                    Sex.MALE, null, null, null);

    // when & then
    NotFoundException thrown = assertThrows(NotFoundException.class, () -> horseDao.update(horse));
    assertThat(thrown.getMessage()).contains("Could not update horse with ID -999, because it does not exist");
  }
}
