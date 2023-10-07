package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import java.util.List;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {
  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();

  /**
   * Create a horse in the persistent data store with the
   * data given in {@code horse}
   *
   * @param horse the horse to create
   * @return the created horse
   */
  Horse create(HorseCreateDto horse);


  /**
   * Update the horse with the ID given in {@code horse}
   *  with the data given in {@code horse}
   *  in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseDetailDto horse) throws NotFoundException;

  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Get the parent by its ID from the persistent data store.
   *
   * @param id the ID of the parent to get
   * @return the parent horse
   * @throws NotFoundException if the parent horse with the given ID does not exist in the persistent data store
   */
  Horse getParentById(long id) throws NotFoundException;

  /**
   * Delete the horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
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
  List<Horse> searchHorses(HorseSearchDto horseSearchDto);

  /**
   * Get all horses that are in the family tree of the horse
   * whose id is given in {@code id}. How many generations
   * are included in the list is specified in {@code generation}
   *
   * @param id the id of the root horse
   * @param generation the number of generations to fetch
   * @return a list of horses that are included in the family tree given the conditions
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  List<Horse> getFamilyTree(long id, int generation) throws NotFoundException;

  /**
   * Get all horses that are children of the horse
   * whose id is given in {@code id}.
   *
   * @param id the id of the parent
   * @return a list of children  horses
   */
  List<Horse> getChildren(long id);
}
