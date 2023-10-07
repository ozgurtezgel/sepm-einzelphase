package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Owner;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles({"test", "datagen"})
@SpringBootTest
public class OwnerDaoTest {

  @Autowired
  OwnerDao ownerDao;

  @Autowired
  DataGeneratorBean dataGeneratorBean;

  @BeforeEach
  public void setUp() throws SQLException {
    dataGeneratorBean.deleteData();
    dataGeneratorBean.generateData();
  }

  @Test
  public void shouldSuccessfullyCreateOwner() {
    // given the current state of db
    // when
    Owner owner = ownerDao.create(
            new OwnerCreateDto("Owner", "Test",
                    "testowner@hotmail.com"));

    // then
    assertAll(
            () -> assertThat(owner.getId()).isNotNull(),
            () -> assertThat(owner.getFirstName()).isEqualTo("Owner"),
            () -> assertThat(owner.getLastName()).isEqualTo("Test"),
            () -> assertThat(owner.getEmail()).isEqualTo("testowner@hotmail.com")
    );
  }

  @Test
  public void shouldSuccessfullyGetOwnerById() throws NotFoundException {
    // given the current state of db
    // when
    Owner owner = ownerDao.getById(-10);

    // then
    assertAll(
            () -> assertThat(owner.getId()).isEqualTo(-10),
            () -> assertThat(owner.getFirstName()).isEqualTo("Pep"),
            () -> assertThat(owner.getLastName()).isEqualTo("Si"),
            () -> assertThat(owner.getEmail()).isEqualTo("pep@si.com")
    );
  }
}
