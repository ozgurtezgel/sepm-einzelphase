import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss']
})
export class HorseDetailComponent implements OnInit {

  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    mother: undefined,
    father: undefined
  };

  constructor(
    private service: HorseService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
  }

  public get heading(): string {
    return 'Details of Horse';
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id !== null) {
      this.service.getById(id)
        .subscribe({
          next: data => {
            console.log(`Horse with ID:${id} is successfully fetched`);
            this.horse = data;
          },
          error: error => {
            console.error('Error fetching the horse', error);
            if (error.status === 404) {
              const errorMessage = 'Horse is not found';
              this.notification.error(errorMessage, 'Could Not Fetch The Horse');
            }
            this.router.navigate(['/horses']);
          }
        });
    }
  }

  public deleteHorse() {
    this.service.delete(this.horse.id).subscribe({
      next: data => {
        console.log(`Horse with ID:${this.horse.id} is successfully deleted`);
        this.notification.success(`Horse ${this.horse.name} successfully deleted`);
        this.router.navigate([`/horses`]);
      },
      error: error => {
        console.error('Error deleting the horse', error);
        if (error.status === 404) {
          const errorMessage = 'Horse to be deleted is not found';
          this.notification.error(errorMessage, 'Error deleting the horse');
        }
      }
    });
  }

  public formatGender(sex: Sex | null | undefined): string {
    switch (sex) {
      case Sex.male:
        return 'Male';
        break;
      case Sex.female:
        return 'Female';
        break;
      default:
        console.error('Unknown Gender', sex);
        return 'Undefined Gender';
    }
  }

  public dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  public getDescription(horse: Horse): string | undefined {
    return horse.description;
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatParentName(parent: Horse | null | undefined): string {
    return (parent == null)
      ? ''
      : `${parent.name}`;
  }

}
