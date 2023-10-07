import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';


export enum HorseCreateEditMode {
  create,
  edit,
};

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {

  mode: HorseCreateEditMode = HorseCreateEditMode.create;
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
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Edit Horse';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Save';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }


  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'edited';
      default:
        return '?';
    }
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
    if (!this.modeIsCreate) {
      this.loadHorseInEditMode();
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  motherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByName(input, 5);

  fatherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByName(input, 5);

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatMotherName(mother: Horse | null | undefined): string {
    return (mother == null)
      ? ''
      : `${mother.name}`;
  }

  public formatFatherName(father: Horse | null | undefined): string {
    return (father == null)
      ? ''
      : `${father.name}`;
  }

  public loadHorseInEditMode(): void {
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
              const errorMessage = error.status === 404 ?
                    'Horse to be edited is not found' : error.message.message;
              this.notification.error(errorMessage, 'Could Not Fetch The Horse');
              console.error('Error: ' + errorMessage);
            }
            this.router.navigate(['/horses']);
          }
        });
      }
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      this.removeEmptyFields();
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          this.createAction();
          break;
        case HorseCreateEditMode.edit:
          this.editAction();
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
    }
  }

  createAction(): void {
    this.service.create(this.horse).subscribe({
      next: data => {
        console.log(`Horse with ID:${data.id} is successfully ${this.modeActionFinished}`);
        this.notification.success(`Horse ${data.name} successfully ${this.modeActionFinished}.`);
        this.router.navigate(['/horses']);
      },
      error: error => {
        console.error('Error creating horse', error);
        if (error.status === 422 || error.status === 409) {
          if (error.error.errors.length > 1) {
            for (const message of error.error.errors) {
              this.notification.error(message);
            }
            this.notification.error('Error creating the horse\n');
          } else {
            this.notification.error(error.error.errors, 'Error creating the horse\n');
          }
        }
      }
    });
  }

  editAction(): void {
    this.service.put(this.horse.id, this.horse).subscribe({
      next: data => {
        console.log(`Horse with ID:${data.id} is successfully ${this.modeActionFinished}`);
        this.notification.success(`Horse ${data.name} successfully ${this.modeActionFinished}.`);
        this.router.navigate([`/horses`]);
      },
      error: error => {
        console.error('Error editing the horse\n', error);
        if (error.status === 422 || error.status === 409) {
          if (error.error.errors.length > 1) {
            for (const message of error.error.errors) {
              this.notification.error(message);
            }
            this.notification.error('Error editing the horse\n');
          } else {
            this.notification.error(error.error.errors, 'Error editing the horse\n');
          }
        }
      }
    });
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

  removeEmptyFields(): void {
    if (this.horse.description === '') {
      delete this.horse.description;
    }
    if (this.horse.owner === undefined) {
      delete this.horse.owner;
    }
    if (this.horse.mother === undefined) {
      delete this.horse.mother;
    }
    if (this.horse.father === undefined) {
      delete this.horse.father;
    }
  }

}
