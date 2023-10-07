package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Owner;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.persistence.OwnerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class OwnerValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
          + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

  private final OwnerDao ownerDao;

  public OwnerValidator(OwnerDao dao) {
    this.ownerDao = dao;
  }

  public void validateForCreate(OwnerCreateDto owner) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({})", owner);
    List<String> validationErrors = new ArrayList<>();

    validateName(validationErrors, owner);
    if (owner.email() != null && !isEmailValid(owner.email())) {
      validationErrors.add("Email of the owner is invalid");
    }

    if (!validationErrors.isEmpty()) {
      LOG.warn("The validation of the owner to create has failed", validationErrors);
      throw new ValidationException("Validation of horse for create failed ", validationErrors);
    }

    List<String> conflictErrors = new ArrayList<>();
    if (!isEmailUnique(owner.email())) {
      conflictErrors.add("Email of the owner must be unique");
    }

    if (owner.email() != null && !conflictErrors.isEmpty()) {
      LOG.warn("There are conflicts caused by the owner to create", conflictErrors);
      throw new ConflictException("There are conflicts in the owner", conflictErrors);
    }
  }

  private void validateName(List validationErrors, OwnerCreateDto owner) {
    LOG.trace("validateName({} {})", owner.firstName(), owner.lastName());
    if (owner.firstName() == null) {
      validationErrors.add("First name of the owner cannot be null");
    }

    if (owner.lastName() == null) {
      validationErrors.add("Last name of the owner cannot be null");
    }
  }

  private boolean isEmailValid(String email) {
    LOG.trace("isEmailValid({})", email);
    return Pattern.compile(REGEX).matcher(email).matches();
  }

  private boolean isEmailUnique(String email) {
    LOG.trace("isEmailUnique({})", email);
    Owner owner;
    owner = ownerDao.getOwnerByEmail(email);
    if (owner != null) {
      return false;
    }
    return true;
  }

}
