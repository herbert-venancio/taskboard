import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProjectProfileComponent} from './config/profile/project-profile.component';
import {ProjectTeamsComponent} from './config/teams/project-teams.component';
import {ProjectClusterComponent} from './config/cluster/project-cluster.component';
import {ProjectChangeRequestsComponent} from './config/changeRequests/project-changeRequests.component';
import {ProjectConfigComponent} from "./config/project-config.component";
import {LeaveConfirmationGuard} from '../shared/form-utils/leave-confirmation/guard/leave-confirmation.guard';

const routes: Routes = [
    {
        path: 'project',
        children: [
            {
                path: ':key/config',
                component: ProjectConfigComponent,
                children: [
                    {
                        path: 'profile',
                        component: ProjectProfileComponent
                    },
                    {
                        path: 'teams',
                        component: ProjectTeamsComponent
                    },
                    {
                        path: 'cluster',
                        component: ProjectClusterComponent,
                        canDeactivate: [ LeaveConfirmationGuard ]
                    },
                    {
                        path: 'changeRequests',
                        component: ProjectChangeRequestsComponent,
                        canDeactivate: [ LeaveConfirmationGuard ]
                    }
                ]
            }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ProjectRoutingModule {
}
