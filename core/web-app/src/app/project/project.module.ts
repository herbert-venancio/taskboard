import {NgModule} from '@angular/core';
import {ProjectRoutingModule} from './project-routing.module';
import {SharedModule} from 'app/shared/shared.module';
import {ProjectProfileComponent} from './config/profile/project-profile.component';
import {ProjectTeamsComponent} from './config/teams/project-teams.component';
import {ProjectClusterComponent} from './config/cluster/project-cluster.component';
import {ProjectChangeRequestsComponent} from './config/changeRequests/project-changeRequests.component';
import {ProjectClusterRecalculateModalComponent} from './config/cluster/project-cluster-recalculate-modal.component';
import {FormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {ProjectConfigComponent} from "./config/project-config.component";

@NgModule({
    imports: [
        NgSelectModule,
        FormsModule,
        SharedModule,
        ProjectRoutingModule
    ],
    declarations: [
        ProjectProfileComponent,
        ProjectTeamsComponent,
        ProjectClusterComponent,
        ProjectClusterRecalculateModalComponent,
        ProjectChangeRequestsComponent,
        ProjectConfigComponent
    ]
})
export class ProjectModule {
}
