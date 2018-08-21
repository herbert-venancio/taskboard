import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProjectProfileComponent} from './config/profile/project-profile.component';
import {ProjectTeamsComponent} from './config/teams/project-teams.component';
import {ProjectConfigComponent} from "./config/project-config.component";

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
