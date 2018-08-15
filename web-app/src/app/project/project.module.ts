import {NgModule} from '@angular/core';
import {ProjectRoutingModule} from './project-routing.module';
import {SharedModule} from 'app/shared/shared.module';
import {ProjectProfileComponent} from './config/profile/project-profile.component';
import {FormsModule} from '@angular/forms';
import {ProjectConfigComponent} from "./config/project-config.component";

@NgModule({
    imports: [
        FormsModule,
        SharedModule,
        ProjectRoutingModule
    ],
    declarations: [
        ProjectProfileComponent,
        ProjectConfigComponent
    ]
})
export class ProjectModule {
}
