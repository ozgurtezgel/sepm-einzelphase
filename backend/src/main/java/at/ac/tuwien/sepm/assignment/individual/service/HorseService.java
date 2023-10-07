package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.ParentDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {
  /**
   * Lists all horses stored in the system.
   *
   * @return list of all stored horses
   */
  Stream<HorseListDto> allHorses();

  /**
   * Creates a horse in the persistent data store with the
   * data given in {@code horse}
   *
   * @param horse the horse to create
   * @return the created horse
   * @throws ValidationException if the create data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException if the create data given for the horse is causing conflicts with the current data in the system
   */
  HorseDetailDto create(HorseCreateDto horse) throws ValidationException, ConflictException;

  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException if the update data given for the horse is causing conflicts with the current data in the system (owner does not exist, …)
   */
  HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException;


  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Get the parent horse with given ID, with only information
   * about name
   *
   * @param id the ID of the parent horse to get
   * @return the parent dto with ID {@code id}
   * @throws NotFoundException if the parent horse with the given ID does not exist in the persistent data store
   */
  ParentDto getParentById(long id) throws NotFoundException;

  /**
   * Deletes the horse with the ID given in {@code id}
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  void deleteById(long id) throws NotFoundException;

  /**
   * Search for horses matching the criteria in {@code horseSearchDto}.
   * <p>
   * A horse is considered matched, if its name contains {@code horseSearchDto.name} as a substring,
   * if its description contains {@code horseSearchDto.name} as a substring, if its date of birth is
   * before {@code horseSearchDto.bornBefore}, if its sex is {@code horseSearchDto.sex}, if its
   * owner name contains {@code horseSearchDto.ownerName} as a substring. If multiple criteria
   * are given, then the results must satisfy every criteria.
   *
   * The returned stream of horses never contains more than {@code horseSearchDto.maxAmount} elements,
   *  even if there would be more matches in the persistent data store.
   * </p>
   *
   * @param horseSearchDto object containing the search parameters to match
   * @return a list of horses matching the criteria in {@code horseSearchDto}
   */
  Stream<HorseListDto> searchHorses(HorseSearchDto horseSearchDto);

  /**
   * Get the HorseFamilyTreeDto that contains all horses that
   * is in the family tree of the horse
   * whose id is given in {@code id}. How many generations
   * are included in the list is specified in {@code generation}
   *
   * @param id the id of the root horse
   * @param generation the number of generations to fetch
   * @return a list of horses that are included in the family tree given the conditions
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  HorseFamilyTreeDto getFamilyTree(long id, int generation) throws NotFoundException;
}
