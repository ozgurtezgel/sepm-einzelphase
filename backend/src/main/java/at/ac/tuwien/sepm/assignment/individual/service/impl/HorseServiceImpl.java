package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.dto.ParentDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    var horses = dao.getAll();
    var ownerIds = horses.stream()
        .map(Horse::getOwnerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  @Override
  public HorseDetailDto create(HorseCreateDto horse) throws ValidationException, ConflictException {
    LOG.trace("create({})", horse);
    validator.validateForCreate(horse);
    Horse createdHorse = dao.create(horse);
    return mapper.entityToDetailDto(
            createdHorse,
            ownerMapForSingleId(createdHorse.getOwnerId()),
            parentMapForSingleId(createdHorse.getMotherId()),
            parentMapForSingleId(createdHorse.getFatherId())
    );
  }

  @Override
  public HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);
    validator.validateForUpdate(horse);
    var updatedHorse = dao.update(horse);
    return mapper.entityToDetailDto(
        updatedHorse,
        ownerMapForSingleId(updatedHorse.getOwnerId()),
        parentMapForSingleId(updatedHorse.getMotherId()),
        parentMapForSingleId(updatedHorse.getFatherId()));
  }

  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(
            horse,
            ownerMapForSingleId(horse.getOwnerId()),
            parentMapForSingleId(horse.getMotherId()),
            parentMapForSingleId(horse.getFatherId()));
  }

  @Override
  public ParentDto getParentById(long id) throws NotFoundException {
    LOG.trace("detailsOfParent({})", id);
    Horse horse = dao.getParentById(id);
    return mapper.entityToParentDto(horse);
  }

  @Override
  public void deleteById(long id) throws NotFoundException {
    LOG.trace("Delete horse with id:{}", id);
    dao.deleteById(id);
  }

  @Override
  public Stream<HorseListDto> searchHorses(HorseSearchDto horseSearchDto) {
    LOG.trace("searchParams({})", horseSearchDto);
    var horses = dao.searchHorses(horseSearchDto);
    var ownerIds = horses.stream()
            .map(Horse::getOwnerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
            .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  @Override
  public HorseFamilyTreeDto getFamilyTree(long id, int generation) throws NotFoundException {
    LOG.trace("getFamilyTree(id:{}, generation:{})", id, generation);
    var horses = dao.getFamilyTree(id, generation);
    return convertListToFamilyTreeDto(horses, id);
  }

  private HorseFamilyTreeDto convertListToFamilyTreeDto(List<Horse> familyList, long id) throws NotFoundException {
    LOG.trace("convertListToFamilyTreeDto({})", familyList);
    List<Horse> horseList = familyList.stream().filter((horse) -> horse.getId() == id).collect(Collectors.toList());
    if (horseList.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    Horse horse = horseList.get(0);
    return buildFamilyTreeDto(horse, familyList);

  }

  private HorseFamilyTreeDto buildFamilyTreeDto(Horse horse, List<Horse> familyList) {
    LOG.trace("buildFamilyTreeDto(root:{})", horse);
    if (horse != null) {
      Horse mother = null;
      Horse father = null;

      for (Horse h : familyList) {
        if (h.getId().equals(horse.getMotherId())) {
          mother = h;
        }
        if (h.getId().equals(horse.getFatherId())) {
          father = h;
        }
      }
      return new HorseFamilyTreeDto(horse.getId(), horse.getName(), horse.getDateOfBirth(), horse.getSex(),
              buildFamilyTreeDto(mother, familyList), buildFamilyTreeDto(father, familyList));
    } else {
      return null;
    }
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    LOG.trace("ownerMapForSingleId({})", ownerId);
    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  private Map<Long, ParentDto> parentMapForSingleId(Long parentId) {
    LOG.trace("parentMapForSingleId({})", parentId);
    try {
      return parentId == null
              ? null
              : Collections.singletonMap(parentId, getParentById(parentId));
    } catch (NotFoundException e) {
      throw new FatalException("Parent %d referenced by horse not found".formatted(parentId));
    }
  }

}
