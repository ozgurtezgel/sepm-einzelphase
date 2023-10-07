package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.persistence.DataGeneratorBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.sql.SQLException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class OwnerEndpointTest {

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
  public void gettingAllOwners() throws Exception {
    // given the current state of db
    // when
    byte[] body = mockMvc
            .perform(MockMvcRequestBuilders
                    .get("/owners")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

    List<OwnerDto> ownerResult = objectMapper.readerFor(OwnerDto.class).<OwnerDto>readValues(body).readAll();

    // then
    assertThat(ownerResult).isNotNull();
    assertThat(ownerResult.size()).isGreaterThanOrEqualTo(10);
  }

  @Test
  public void shouldSuccessfullyCreateOwner() throws Exception {
    // given the current state of db
    String ownerJsonString = "{\"firstName\": \"Owner\",\n"
            + "\"lastName\": \"Test\","
            + "\"email\": \"ownertest@gmail.com\"}";

    // when
    byte[] body = mockMvc.perform(MockMvcRequestBuilders
                    .post("/owners")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ownerJsonString)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsByteArray();

    List<OwnerDto> ownerResult = objectMapper.readerFor(OwnerDto.class).<OwnerDto>readValues(body).readAll();
    OwnerDto owner = ownerResult.get(0);

    // then
    assertAll(
            () -> assertThat(owner.id()).isNotNull(),
            () -> assertThat(owner.firstName()).isEqualTo("Owner"),
            () -> assertThat(owner.lastName()).isEqualTo("Test"),
            () -> assertThat(owner.email()).isEqualTo("ownertest@gmail.com")
    );
  }
}
