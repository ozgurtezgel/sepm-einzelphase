import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Owner } from 'src/app/dto/owner';
import { OwnerService } from 'src/app/service/owner.service';


@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.scss']
})
export class OwnerComponent implements OnInit {
  owners: Owner[] = [];


  constructor(
    private service: OwnerService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadOwners();
  }

  reloadOwners() {
    this.service.getAllOwners()
      .subscribe({
        next: data => {
          console.log(`Owners are successfully fetched\n`, data);
          this.owners = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          const errorMessage = 'Is the backend running?';
          this.notification.error(errorMessage, 'Could Not Fetch Owners');
        }
      });
  }
}
