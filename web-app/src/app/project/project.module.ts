import {NgModule} from '@angular/core';
import {ProjectRoutingModule} from './project-routing.module';
import {SharedModule} from 'app/shared/shared.module';
import {ProjectProfileComponent} from './config/project-profile.component';

@NgModule({
    imports: [
        SharedModule,
        ProjectRoutingModule
    ],
    declarations: [
        ProjectProfileComponent
    ],
    exports: [
        ProjectProfileComponent
    ]
})
export class ProjectModule {
}
