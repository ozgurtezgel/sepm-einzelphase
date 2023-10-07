import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {ToastrModule} from 'ngx-toastr';

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {AutocompleteComponent} from './component/autocomplete/autocomplete.component';
import {HeaderComponent} from './component/header/header.component';
import {HorseCreateEditComponent} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import { HorseDetailComponent } from './component/horse/horse-detail/horse-detail.component';
import { OwnerComponent } from './component/owner/owner.component';
import { OwnerCreateComponent } from './component/owner/owner-create/owner-create.component';
import { HorseFamilyTreeComponent } from './component/horse/horse-family-tree/horse-family-tree.component';
import { FamilyTreeNodeComponent } from './component/horse/horse-family-tree/horse-family-tree/family-tree-node/family-tree-node.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    HorseComponent,
    HorseCreateEditComponent,
    AutocompleteComponent,
    HorseDetailComponent,
    OwnerComponent,
    OwnerCreateComponent,
    HorseFamilyTreeComponent,
    FamilyTreeNodeComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ToastrModule.forRoot(),
    // Needed for Toastr
    BrowserAnimationsModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
