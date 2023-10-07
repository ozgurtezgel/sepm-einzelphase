package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao horseDao;
  private final OwnerService ownerService;

  public HorseValidator(HorseDao dao, OwnerService ownerService) {
    this.horseDao = dao;
    this.ownerService = ownerService;
  }


  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }

    validateAll(validationErrors, horse.name(), horse.description(), horse.dateOfBirth(),
            horse.sex());

    if (!validationErrors.isEmpty()) {
      LOG.warn("The validation of the horse to update has failed", validationErrors);
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

    List<String> conflictErrors = new ArrayList<>();
    if (horse.id() == horse.motherId()) {
      conflictErrors.add("Horse cannot be its own mother");
    }
    if (horse.id() == horse.fatherId()) {
      conflictErrors.add("Horse cannot be its own father");
    }

    checkOwner(conflictErrors, horse.ownerId());
    checkMother(conflictErrors, horse.dateOfBirth(), horse.motherId());
    checkFather(conflictErrors, horse.dateOfBirth(), horse.fatherId());
    checkOverallState(conflictErrors, horse);


    if (!conflictErrors.isEmpty()) {
      LOG.warn("There are conflicts caused by the horse to update", conflictErrors);
      throw new ConflictException("There are conflicts in the updated horse", conflictErrors);
    }
  }

  public void validateForCreate(HorseCreateDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    validateAll(validationErrors, horse.name(), horse.description(), horse.dateOfBirth(),
            horse.sex());

    if (!validationErrors.isEmpty()) {
      LOG.warn("The validation of the horse to create has failed", validationErrors);
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }

    List<String> conflictErrors = new ArrayList<>();
    checkOwner(conflictErrors, horse.ownerId());
    checkMother(conflictErrors, horse.dateOfBirth(), horse.motherId());
    checkFather(conflictErrors, horse.dateOfBirth(), horse.fatherId());


    if (!conflictErrors.isEmpty()) {
      LOG.warn("There are conflicts caused by the horse to create", conflictErrors);
      throw new ConflictException("There are conflicts in the created horse", conflictErrors);
    }
  }

  private void validateAll(List<String> validationErrors, String name, String description,
                           LocalDate localDate, Sex sex) {
    validateName(validationErrors, name);
    validateDescription(validationErrors, description);
    validateDateOfBirth(validationErrors, localDate);
    validateSex(validationErrors, sex);
  }

  private void checkOverallState(List<String> conflictErrors, HorseDetailDto horse) throws NotFoundException {
    LOG.trace("checkOverallState({})", horse);
    List<Horse> children = horseDao.getChildren(horse.id());

    if (!children.isEmpty()) {
      Horse horseBeforeChange = horseDao.getById(horse.id());

      if (!horseBeforeChange.getSex().equals(horse.sex())) {
        conflictErrors.add("The sex of the horse cannot be changed, as it has children");
      }
      if (!horseBeforeChange.getDateOfBirth().equals(horse.dateOfBirth())) {
        conflictErrors.add("The date of birth of the horse cannot be changed, as it has children");
      }
    }
  }

  private void checkOwner(List<String> conflictErrors, Long ownerId) {
    LOG.trace("checkOwner({})", ownerId);
    if (ownerId != null) {
      try {
        ownerService.getById(ownerId);
      } catch (NotFoundException e) {
        conflictErrors.add("The given owner does not exist");
      }
    }
  }

  private void checkMother(List<String> conflictErrors, LocalDate childDateOfBirth, Long motherId) {
    LOG.trace("checkMother({})", motherId);
    Horse mother = null;
    if (motherId != null) {
      try {
        mother = horseDao.getParentById(motherId);
      } catch (NotFoundException e) {
        conflictErrors.add("The given mother does not exist");
        return;
      }

      if (!mother.getSex().equals(Sex.FEMALE)) {
        conflictErrors.add("Gender of the mother is not Female!");
      }
      if (!isParentsDateOfBirthValid(childDateOfBirth, mother.getDateOfBirth())) {
        conflictErrors.add("The child cannot be older than the mother");
      }
    }
  }

  private void checkFather(List<String> conflictErrors, LocalDate childDateOfBirth, Long fatherId) {
    LOG.trace("checkFather({})", fatherId);
    Horse father = null;
    if (fatherId != null) {
      try {
        father = horseDao.getParentById(fatherId);
      } catch (NotFoundException e) {
        conflictErrors.add("The given father does not exist");
        return;
      }

      if (!father.getSex().equals(Sex.MALE)) {
        conflictErrors.add("Gender of the father is not Male!");
      }
      if (!isParentsDateOfBirthValid(childDateOfBirth, father.getDateOfBirth())) {
        conflictErrors.add("The child cannot be older than the father");
      }
    }
  }

  private void validateName(List validationErrors, String name) {
    LOG.trace("validateName({})", name);
    if (name != null) {
      if (name.isBlank()) {
        validationErrors.add("Name of the horse cannot be blank");
      }
    } else {
      validationErrors.add("Name of the horse cannot be null");
    }
  }

  private void validateDescription(List validationErrors, String description) {
    LOG.trace("validateDescription({})", description);
    if (description != null) {
      if (description.isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (description.length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }
  }

  private void validateDateOfBirth(List validationErrors, LocalDate dateOfBirth) {
    LOG.trace("validateDateOfBirth({})", dateOfBirth);
    if (dateOfBirth != null) {
      if (dateOfBirth.isAfter(LocalDate.now())) {
        validationErrors.add("Date of birth of the horse cannot be in the future");
      }
    } else {
      validationErrors.add("Date of birth of the horse cannot be null");
    }
  }

  private void validateSex(List<String> validationErrors, Sex sex) {
    LOG.trace("validateSex({})", sex);
    if (sex == null) {
      validationErrors.add("Sex of the horse cannot be null");
    }
  }

  private boolean isParentsDateOfBirthValid(LocalDate childDateOfBirth, LocalDate parentDateOfBirth) {
    LOG.trace("isParentsDateOfBirthValid(childDateOfBirth:{}, parentDateOfBirth:{})", childDateOfBirth, parentDateOfBirth);
    return childDateOfBirth.isAfter(parentDateOfBirth);
  }
}
