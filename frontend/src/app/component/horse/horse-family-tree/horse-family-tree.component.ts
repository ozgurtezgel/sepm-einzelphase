import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HorseService } from 'src/app/service/horse.service';
import {Horse, HorseFamilyTree} from 'src/app/dto/horse';
import { Sex } from 'src/app/dto/sex';
import {ToastrService} from 'ngx-toastr';


@Component({
  selector: 'app-horse-family-tree',
  templateUrl: './horse-family-tree.component.html',
  styleUrls: ['./horse-family-tree.component.scss']
})
export class HorseFamilyTreeComponent implements OnInit {

  generations = 0;
  familyTree: HorseFamilyTree = {
    id: 0,
    name: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    mother: null,
    father: null,
  };

  constructor(
    private service: HorseService,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private router: Router,
  ) {}

  public get heading(): string {
    return 'Family Tree';
  }

  ngOnInit() {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    const id = this.route.snapshot.paramMap.get('id');
    if (id !== null) {
      this.route.queryParams
      .subscribe(params => {
        this.generations = params.generations;
    });
    this.service.getFamilyTree(id, this.generations).subscribe({
      next: data => {
        console.log(`Horse tree is successfully fetched\n`, data);
        this.familyTree = data;
      },
      error: error => {
        console.error('Error fetching the family tree', error);
        if (error.status === 404) {
          const errorMessage = 'Horse whose family tree is fetched is not found';
          this.notification.error(errorMessage, 'Error fetching the family tree');
        } else {
          this.notification.error('Error fetching the family tree');
        }
        this.router.navigate(['/horses']);
      }
    });
    }
  }

  updateFamilyTree(): void {
    this.router.navigate([],
      {
        relativeTo: this.route,
        queryParams: {generations: this.generations},
      });
  }


  reloadHorses(): void {
    this.service.getFamilyTree(this.familyTree.id.toString(), this.generations).subscribe({
      next: data => {
        console.log(`Horse tree is successfully fetched\n`, data);
        this.familyTree = data;
      },
      error: error => {
        console.error('Error fetching the family tree', error);
        if (error.status === 404) {
          const errorMessage = 'Horse whose family tree is fetched is not found';
          this.notification.error(errorMessage, 'Error fetching the family tree');
        } else {
          this.notification.error('Error fetching the family tree');
        }
        this.router.navigate(['/horses']);
      }
    });
  }

  onDeleted(horseToBeDeleted: HorseFamilyTree): void {
    this.service.delete(horseToBeDeleted.id).subscribe({
      next: data => {
        console.log(`Horse with id:${horseToBeDeleted.id} is deleted`);
        this.notification.success(`Horse ${horseToBeDeleted.name} successfully deleted`);
        this.reloadHorses();
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
}

