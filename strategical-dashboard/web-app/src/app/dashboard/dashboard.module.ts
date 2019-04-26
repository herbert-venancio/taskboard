import { NgModule } from '@angular/core';
import { DashboardRoutingModule } from './dashboard.routing.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ObjectiveDesignSystemModule } from 'objective-design-system';
import { ProjectModule } from './dashboard/project/project.module';


@NgModule({
    imports: [
        DashboardRoutingModule,
        ObjectiveDesignSystemModule,
        ProjectModule
    ],
    declarations: [
        DashboardComponent
    ],
    exports: [
    ]
})

export class DashboardModule {
}
