import {NgModule} from '@angular/core';
import {ProjectRoutingModule} from './project-routing.module';
import {SharedModule} from 'app/shared/shared.module';

@NgModule({
    imports: [
        SharedModule,
        ProjectRoutingModule
    ],
    declarations: [
    ]
})
export class ProjectModule {
}
