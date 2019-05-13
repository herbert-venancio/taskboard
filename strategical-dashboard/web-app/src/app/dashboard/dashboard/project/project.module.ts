import { NgModule } from '@angular/core';
import { ObjectiveDesignSystemModule } from 'objective-design-system';
import { MonitorModule } from '../monitor/monitor.module';
import { ProjectItemComponent } from './project/project-item.component';

@NgModule({
    imports: [
        ObjectiveDesignSystemModule,
        MonitorModule
    ],
    declarations: [
        ProjectItemComponent
    ],
    exports: [
        ProjectItemComponent
    ]
})

export class ProjectModule {}