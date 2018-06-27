import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProjectProfileComponent} from './config/project-profile.component';

const routes: Routes = [
    {
        path: 'project',
        children: [
            {path: ':key/config/profile', component: ProjectProfileComponent}
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ProjectRoutingModule {
}
