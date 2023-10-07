import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Horse, HorseFamilyTree } from 'src/app/dto/horse';
import { Sex } from 'src/app/dto/sex';
import { HorseService } from 'src/app/service/horse.service';

@Component({
  selector: 'app-family-tree-node',
  templateUrl: './family-tree-node.component.html',
  styleUrls: ['./family-tree-node.component.scss']
})
export class FamilyTreeNodeComponent implements OnInit {

  @Input() horse: HorseFamilyTree | null = {
    id: 0,
    name: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    mother: null,
    father: null,
  };
  @Output() deleted: EventEmitter<HorseFamilyTree> = new EventEmitter();

  constructor(
    private service: HorseService,
    private notification: ToastrService,
  ) {}

  ngOnInit() {
  }

  deleteHorse(horse: HorseFamilyTree): void {
    this.deleted.emit(horse);
  }

  dateOfBirthAsLocaleDate(horse: HorseFamilyTree): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }
}
