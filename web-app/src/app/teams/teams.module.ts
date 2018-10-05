import {NgModule} from '@angular/core';
import {TeamsRoutingModule} from './teams-routing.module';
import {SharedModule} from 'app/shared/shared.module';
import {FormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {TeamsComponent} from './teams/teams.component';
import {TeamComponent} from './teams/team/team.component';

@NgModule({
    imports: [
        NgSelectModule,
        FormsModule,
        SharedModule,
        TeamsRoutingModule
    ],
    declarations: [
        TeamsComponent,
        TeamComponent
    ]
})
export class TeamsModule {
}
