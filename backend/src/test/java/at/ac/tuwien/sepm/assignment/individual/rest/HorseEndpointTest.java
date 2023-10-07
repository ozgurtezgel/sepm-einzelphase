package at.ac.tuwien.sepm.assignment.individual.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.persistence.DataGeneratorBean;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  DataGeneratorBean dataGeneratorBean;

  @BeforeEach
  public void setup() throws SQLException {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    dataGeneratorBean.deleteData();
    dataGeneratorBean.generateData();
  }

  @Test
  public void gettingAllHorses() throws Exception {
    // given the current state of db
    // when
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    // then
    assertThat(horseResult).isNotNull();
    assertThat(horseResult.size()).isGreaterThanOrEqualTo(10);
    assertThat(horseResult)
        .extracting(HorseListDto::id, HorseListDto::name)
            .contains(tuple(-1L, "Wendy"), tuple(-2L, "Baba"),
                    tuple(-3L, "Mendy"), tuple(-4L, "Bella"),
                    tuple(-5L, "JJJ"), tuple(-6L, "George"),
                    tuple(-7L, "Bullseye"), tuple(-8L, "Plane"),
                    tuple(-9L, "Pegasus"), tuple(-10L, "THY"));
  }

  @Test
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/asdf123")
        ).andExpect(status().isNotFound());
  }

  @Test
  public void shouldSuccessfullyCreateHorse() throws Exception {
    // given the current state of db
    String horseJsonString = "{\"name\": \"Horse\",\n"
            + "\"description\": \"EndPoint Test create\","
            + "\"dateOfBirth\": \"2010-10-10\","
            + "\"sex\": \"FEMALE\"}";

    // when
    byte[] body = mockMvc.perform(MockMvcRequestBuilders
                    .post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(horseJsonString)
                    .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsByteArray();

    List<HorseDetailDto> horseResult = objectMapper.readerFor(HorseDetailDto.class).<HorseDetailDto>readValues(body).readAll();
    HorseDetailDto horse = horseResult.get(0);

    // then
    assertThat(horse).isNotNull();
    assertThat(horse.id()).isNotNull();
    assertThat(horse)
            .extracting(HorseDetailDto::name, HorseDetailDto::description, HorseDetailDto::dateOfBirth, HorseDetailDto::sex)
            .contains("Horse", "EndPoint Test create", LocalDate.parse("2010-10-10"), Sex.FEMALE);
  }

  @Test
  public void shouldSuccessfullyUpdateHorse() throws Exception {
    // given the current state of db
    String horseJsonString = "{\"name\": \"Pegasus II\",\n"
            + "\"description\": \"Not Daughter\","
            + "\"dateOfBirth\": \"2018-12-12\","
            + "\"sex\": \"FEMALE\"}";

    // when
    byte[] body = mockMvc.perform(MockMvcRequestBuilders
                    .put("/horses/-9")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(horseJsonString)
                    .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

    List<HorseDetailDto> horseResult = objectMapper.readerFor(HorseDetailDto.class).<HorseDetailDto>readValues(body).readAll();
    HorseDetailDto horse = horseResult.get(0);

    // then
    assertAll(
            () -> assertThat(horse).isNotNull(),
            () -> assertThat(horse.id()).isEqualTo(-9),
            () -> assertThat(horse)
                    .extracting(HorseDetailDto::name, HorseDetailDto::description, HorseDetailDto::dateOfBirth, HorseDetailDto::sex)
                    .contains("Pegasus II", "Not Daughter", LocalDate.parse("2018-12-12"), Sex.FEMALE)
    );
  }

  @Test
  @DisplayName("should throw conflict exception when the gender and date of birth "
          + "of the parent is changed")
  public void shouldThrowConflictExceptionWhenCriticalDataOfParentIsChanged() throws Exception {
    // given the current state of db
    String horseJsonString = "{\"name\": \"George\",\n"
            + "\"description\": \"vet\","
            + "\"dateOfBirth\": \"2018-12-12\","
            + "\"sex\": \"FEMALE\"}";

    // when & then
    mockMvc.perform(MockMvcRequestBuilders
                    .put("/horses/-6")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(horseJsonString)
                    .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict())
            .andExpect(result -> assertThat(result.getResolvedException() instanceof ConflictException))
            .andExpect(result -> assertTrue(result.getResolvedException().getMessage()
                    .contains("The sex of the horse cannot be changed, as it has children, "
                            + "The date of birth of the horse cannot be changed, as it has children.")));
  }
}
