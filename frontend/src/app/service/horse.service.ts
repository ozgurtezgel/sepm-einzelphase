import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import { HorseDetailComponent } from '../component/horse/horse-detail/horse-detail.component';
import {Horse, HorseFamilyTree, HorseSearch} from '../dto/horse';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient,
  ) { }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri);
  }

  /**
   * Get the horse with the specified id
   *
   * @param id the id of the horse that should be fetched
   * @return an Observable for the fetched horse
   */
  getById(id: string): Observable<Horse> {
    return this.http.get<Horse>(baseUri + `/${id}`);
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: Horse): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  /**
   * Get the horse with the specified id
   *
   * @param id the id of the horse that should be updated
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  put(id: number | undefined, horse: Horse): Observable<Horse> {
    return this.http.put<Horse>(baseUri + `/${id}`, horse);
  }

  /**
   * Delete the horse with the specified id
   *
   * @param id the id of the horse that should be deleted
   */
  delete(id: number | undefined): Observable<void> {
    return this.http.delete<void>(baseUri + `/${id}`);
  }

  /**
   * Get horses according to speicfied name
   *
   * @param name the name of the horse that should be fetched
   * @param limitTo max number of horses that should be fetched
   */
  public searchByName(name: string, limitTo: number): Observable<Horse[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('maxAmount', limitTo);
    return this.http.get<Horse[]>(baseUri, { params });
  }

  /**
   * Get horses according to speicfied parameters
   *
   * @param searchParams the search parameters that should be used
   * to search/get horses
   */
  public searchHorse(searchParams: HorseSearch): Observable<Horse[]> {
    let params = new HttpParams();
    if (searchParams.name) {
      params = params.set('name', searchParams.name);
    }
    if (searchParams.description) {
      params = params.set('description', searchParams.description);
    }
    if (searchParams.bornBefore) {
      params = params.set('bornBefore', searchParams.bornBefore);
    }
    if (searchParams.sex) {
      params = params.set('sex', searchParams.sex);
    }
    if (searchParams.ownerName) {
      params = params.set('ownerName', searchParams.ownerName);
    }
    return this.http.get<Horse[]>(baseUri, { params });
  }

  /**
   * Get the family tree of the horse with the specified id
   *
   * @param id the id of the horse whose family tree should be fetched
   * @param generations the number of generations that should be fetched
   * @return an Observable for the fetched family tree
   */
  getFamilyTree(id: string, generations: number): Observable<HorseFamilyTree> {
    return this.http.get<HorseFamilyTree>(baseUri + `/${id}/familytree/${generations}`);
  }
}
