package at.ac.tuwien.sepm.assignment.individual.persistence.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";
  private static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , description = ?"
      + "  , date_of_birth = ?"
      + "  , sex = ?"
      + "  , owner_id = ?"
      + "  , mother_id = ?"
      + "  , father_id = ?"
      + " WHERE id = ?";

  private static final String SQL_CREATE = "INSERT INTO " + TABLE_NAME
          + "(name, description, date_of_birth, sex, owner_id, mother_id, father_id) "
          + "VALUES (?,?,?,?,?,?,?)";

  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_UPDATE_CHILDREN_WHEN_DELETING_MOTHER = "UPDATE " + TABLE_NAME
          + " SET mother_id = null WHERE mother_id = ?";

  private static final String SQL_UPDATE_CHILDREN_WHEN_DELETING_FATHER = "UPDATE " + TABLE_NAME
          + " SET father_id = null WHERE father_id = ?";

  private static final String SQL_SEARCH = "SELECT horse.* FROM " + TABLE_NAME
          + " LEFT JOIN owner ON horse.owner_id=owner.id";

  private static final String SQL_FAMILY_TREE = "WITH RECURSIVE"
          + " tmp(id, name, date_of_birth, sex, mother_id, father_id, generation)"
          + " AS ("
          + " SELECT id, name, date_of_birth, sex, mother_id, father_id, 1 FROM horse WHERE id = ?"
          + " UNION"
          + " SELECT h.id, h.name, h.date_of_birth, h.sex, h.mother_id, h.father_id, tmp.generation + 1"
          + " FROM horse h JOIN tmp ON tmp.mother_id = h.id OR tmp.father_id = h.id)"
          + " SELECT id, name, date_of_birth, sex, mother_id, father_id, generation FROM tmp WHERE generation <= ?";

  private static final String SQL_GET_CHILDREN = "SELECT * FROM " + TABLE_NAME
          + " WHERE mother_id = ? OR father_id = ?";

  private final JdbcTemplate jdbcTemplate;

  public HorseJdbcDao(
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcTemplate.query(SQL_SELECT_ALL, this::mapRow);
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      LOG.error("Too many horses with ID %d found".formatted(id));
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public Horse getParentById(long id) throws NotFoundException {
    LOG.trace("getParentById({})", id);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public Horse create(HorseCreateDto horse) {
    LOG.trace("create({})", horse);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      PreparedStatement stmt = con.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, horse.name());
      stmt.setString(2, horse.description());
      stmt.setString(3, horse.dateOfBirth().toString());
      stmt.setString(4, horse.sex().toString());
      stmt.setString(5, horse.ownerId() == null ? null : horse.ownerId().toString());
      stmt.setString(6, horse.motherId() == null ? null : horse.motherId().toString());
      stmt.setString(7, horse.fatherId() == null ? null : horse.fatherId().toString());
      return stmt;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      LOG.error("Could not extract key for newly created horse");
      throw new FatalException("Could not extract key for newly created horse.");
    }

    return new Horse()
            .setId(key.longValue())
            .setName(horse.name())
            .setDescription(horse.description())
            .setDateOfBirth(horse.dateOfBirth())
            .setSex(horse.sex())
            .setOwnerId(horse.ownerId())
            .setMotherId(horse.motherId())
            .setFatherId(horse.fatherId());
  }

  @Override
  public Horse update(HorseDetailDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcTemplate.update(SQL_UPDATE,
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex().toString(),
        horse.ownerId(),
        horse.motherId(),
        horse.fatherId(),
        horse.id());
    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }

    return new Horse()
        .setId(horse.id())
        .setName(horse.name())
        .setDescription(horse.description())
        .setDateOfBirth(horse.dateOfBirth())
        .setSex(horse.sex())
        .setOwnerId(horse.ownerId())
        .setMotherId(horse.motherId())
        .setFatherId(horse.fatherId())
        ;
  }

  @Override
  public void deleteById(long id) throws NotFoundException {
    LOG.trace("Delete horse with id:{}", id);

    jdbcTemplate.update(SQL_UPDATE_CHILDREN_WHEN_DELETING_MOTHER, id);
    jdbcTemplate.update(SQL_UPDATE_CHILDREN_WHEN_DELETING_FATHER, id);

    int update = jdbcTemplate.update(SQL_DELETE, id);
    if (update == 0) {
      throw new NotFoundException("Could not delete horse with ID " + id + ", because it does not exist");
    }
  }

  @Override
  public List<Horse> searchHorses(HorseSearchDto horseSearchDto) {
    LOG.trace("searchHorses({})", horseSearchDto);
    String query = SQL_SEARCH;
    List params = new ArrayList();

    if (horseSearchDto.name() != null) {
      params.add(horseSearchDto.name());
      query = addNameToQuery(query);
    }

    if (horseSearchDto.description() != null) {
      params.add(horseSearchDto.description());
      query = addDescriptionToQuery(query, params);
    }

    if (horseSearchDto.bornBefore() != null) {
      params.add(horseSearchDto.bornBefore());
      query = addBornBeforeToQuery(query, params);
    }

    if (horseSearchDto.sex() != null) {
      params.add(horseSearchDto.sex().toString());
      query = addSexToQuery(query, params);
    }

    if (horseSearchDto.ownerName() != null) {
      params.add(horseSearchDto.ownerName());
      query = addOwnerNameToQuery(query, params);
    }

    if (horseSearchDto.maxAmount() != null) {
      params.add(horseSearchDto.maxAmount());
      query = query.concat(" LIMIT ?");
    }

    List<Horse> horses;
    System.out.println(query);
    horses = jdbcTemplate.query(query, this::mapRow, params.toArray());
    return horses;
  }

  @Override
  public List<Horse> getFamilyTree(long id, int generation) throws NotFoundException {
    LOG.trace("getFamilyTree(id:{}, generation:{})", id, generation);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_FAMILY_TREE, this::mapRowSec, id, generation);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }

    return horses;
  }

  @Override
  public List<Horse> getChildren(long id) {
    LOG.trace("getChildren(id:{})", id);
    List<Horse> horses = jdbcTemplate.query(SQL_GET_CHILDREN, this::mapRowSec, id, id);

    return horses;
  }

  private String addOwnerNameToQuery(String query, List params) {
    LOG.trace("Adding owner name to search query({})", query);
    if (params.size() > 1) {
      return query.concat(" AND UPPER(owner.first_name ||' '|| owner.last_name) LIKE UPPER('%' || COALESCE(?, '') || '%')");
    } else {
      return query.concat(" WHERE UPPER(owner.first_name ||' '|| owner.last_name) LIKE UPPER('%' || COALESCE(?, '') || '%')");
    }
  }

  private String addSexToQuery(String query, List params) {
    LOG.trace("Adding sex to search query({})", query);
    if (params.size() > 1) {
      return query.concat(" AND horse.sex = ?");
    } else {
      return query.concat(" WHERE horse.sex = ?");
    }
  }

  private String addBornBeforeToQuery(String query, List params) {
    LOG.trace("Adding date of birth to search query({})", query);
    if (params.size() > 1) {
      return query.concat(" AND horse.date_of_birth <= ?");
    } else {
      return query.concat(" WHERE horse.date_of_birth <= ?");
    }
  }

  private String addDescriptionToQuery(String query, List params) {
    LOG.trace("Adding description to search query({})", query);
    if (params.size() > 1) {
      return query.concat(" AND UPPER(horse.description) LIKE UPPER('%' || COALESCE(?, '') || '%')");
    } else {
      return query.concat(" WHERE UPPER(horse.description) LIKE UPPER('%' || COALESCE(?, '') || '%')");
    }
  }

  private String addNameToQuery(String query) {
    LOG.trace("Adding name to search query({})", query);
    return query.concat(" WHERE UPPER(horse.name) LIKE UPPER('%' || COALESCE(?, '') || '%')");
  }

  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDescription(result.getString("description"))
        .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
        .setSex(Sex.valueOf(result.getString("sex")))
        .setOwnerId(result.getObject("owner_id", Long.class))
        .setMotherId(result.getObject("mother_id", Long.class))
        .setFatherId(result.getObject("father_id", Long.class))
        ;
  }

  private Horse mapRowSec(ResultSet result, int rownum) throws SQLException {
    return new Horse()
            .setId(result.getLong("id"))
            .setName(result.getString("name"))
            .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
            .setSex(Sex.valueOf(result.getString("sex")))
            .setMotherId(result.getObject("mother_id", Long.class))
            .setFatherId(result.getObject("father_id", Long.class))
            ;
  }
}
