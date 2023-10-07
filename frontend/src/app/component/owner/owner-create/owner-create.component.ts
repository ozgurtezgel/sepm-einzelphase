import { Component } from '@angular/core';
import { Owner } from 'src/app/dto/owner';
import { OwnerService } from 'src/app/service/owner.service';
import {NgForm, NgModel} from '@angular/forms';
import {ToastrService} from 'ngx-toastr';
import {Router} from '@angular/router';
import {Observable} from 'rxjs';



@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent {

  owner: Owner = {
    firstName: '',
    lastName: '',
    email: ''
  };

  constructor(
    private service: OwnerService,
    private notification: ToastrService,
    private router: Router,
  ) { }

  public get heading(): string {
    return 'Create New Owner';
  }

  public get submitButtonText(): string {
    return 'Create';
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.owner);
    if (form.valid) {
      if (this.owner.email === '') {
        delete this.owner.email;
      }
      let observable: Observable<Owner>;
      this.service.create(this.owner).subscribe({
        next: data => {
          console.log(`Owner ${this.owner.firstName}  
          ${this.owner.firstName} is successfully created`);
          this.notification.success(`Owner ${this.owner.firstName} 
          ${this.owner.lastName} successfully created.`);
          this.router.navigate(['/owners']);
        },
        error: error => {
          console.error('Error creating owner', error);
          if (error.status === 422 || error.status === 409) {
            if (error.error.errors.length > 1) {
              for (const message of error.error.errors) {
                this.notification.error(message);
              }
              this.notification.error('Error creating the owner');
            } else {
              this.notification.error(error.error.errors, 'Error creating the owner\n');
            }
          }
        }
      });
    }
  }
}
