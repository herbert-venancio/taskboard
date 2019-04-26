import { NgModule } from '@angular/core';
import { ProjectRoutingModule } from './project.routing.module';
import { ObjectiveDesignSystemModule } from 'objective-design-system';
import { MonitorModule } from '../monitor/monitor.module';
import { ProjectItemComponent } from './project/project-item.component';

@NgModule({
    imports: [
        ObjectiveDesignSystemModule,
        MonitorModule,
        ProjectRoutingModule
    ],
    declarations: [
        ProjectItemComponent
    ],
    exports: [
        ProjectItemComponent
    ]
})

export class ProjectModule {};