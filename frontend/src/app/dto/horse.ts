import {Owner} from './owner';
import {Sex} from './sex';

export interface Horse {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: Owner;
  mother?: Parent;
  father?: Parent;
}


export interface HorseSearch {
  name?: string;
  description?: string;
  bornBefore?: string;
  sex?: Sex;
  ownerName?: string;
}

export interface HorseFamilyTree {
  id: number;
  name: string;
  dateOfBirth: Date;
  sex: Sex;
  mother: HorseFamilyTree | null;
  father: HorseFamilyTree | null;
}

export interface Parent {
  id: number;
  name: string;
}
