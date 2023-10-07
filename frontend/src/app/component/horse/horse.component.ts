import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import { debounceTime, Subject, takeUntil, of, map} from 'rxjs';
import {HorseService} from 'src/app/service/horse.service';
import { OwnerService } from 'src/app/service/owner.service';
import {Horse, HorseSearch} from '../../dto/horse';
import {Owner} from '../../dto/owner';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {

  search = false;
  horses: Horse[] = [];
  bannerError: string | null = null;
  searchParams: HorseSearch = {
    name: '',
    description: '',
    bornBefore: '',
    sex: undefined,
    ownerName: ''
  };
  searchNotifier = new Subject();

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private ownerService: OwnerService
  ) { }

  ngOnInit(): void {
    this.getAllHorses();
    this.searchNotifier.pipe(debounceTime(500))
      .subscribe(data => this.searchHorse());
  }

  getAllHorses() {
    this.service.getAll()
      .subscribe({
        next: data => {
          console.log(`Horses are successfully fetched\n`, data);
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.notification.error('Is the backend up?', 'Could Not Fetch Horses');
        }
      });
  }

  searchHorse(): void {
    this.removeEmptyFields();
    this.service.searchHorse(this.searchParams)
      .pipe(takeUntil(this.searchNotifier))
      .subscribe({
        next: data => {
          console.log(`Search results are successfully fetched\n`, data);
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.notification.error('Is the backend running?', 'Could Not Fetch Horses');
          this.horses = [];
      }
    });
  }

  public ownerName(owner: Owner | null | undefined): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  public formatOwnerName(ownerName: string): string {
    return (ownerName == null) ? '' : ownerName;
  }

  ownerSuggestions = (input: string) => (input === '')
  ? of([])
  : this.ownerService.searchByName(input, 5).pipe(
    map(owners => owners.map(owner => owner.firstName + ' ' + owner.lastName))
  );

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  deleteHorse(horse: Horse): void {
    this.service.delete(Number(horse.id)).subscribe({
      next: data => {
        console.log(`Horse with ID:${horse.id} is successfully deleted`);
        this.notification.success(`Horse ${horse.name} successfully deleted`);
        this.horses = this.horses.filter(h => h !== horse);
      },
      error: error => {
        console.error('Error deleting the horse', error);
        if (error.status === 404) {
          this.notification.error(error.error.message, 'Error deleting the horse');
        }
      }
    });
  }

  removeEmptyFields(): void {
    if (this.searchParams.name === '') {
      delete this.searchParams.name;
    }
    if (this.searchParams.description === '') {
      delete this.searchParams.description;
    }
    if (this.searchParams.bornBefore === '') {
      delete this.searchParams.bornBefore;
    }
    if (this.searchParams.sex === undefined) {
      delete this.searchParams.sex;
    }
    if (this.searchParams.ownerName === '') {
      delete this.searchParams.ownerName;
    }
  }
}
