import { NgModule } from '@angular/core';
import { ObjectiveDesignSystemModule } from 'objective-design-system';
import { MonitorItemComponent } from './monitor-item/monitor-item.component';
import { MonitorIconComponent } from './monitor-icon/monitor-icon.component';
import { MonitorCostComponent } from './monitor-cost/monitor-cost.component';

@NgModule({
    imports: [
        ObjectiveDesignSystemModule
    ],
    declarations: [
        MonitorItemComponent,
        MonitorIconComponent,
        MonitorCostComponent
    ],
    exports: [
        MonitorItemComponent,
        MonitorIconComponent,
        MonitorCostComponent
    ]
})

export class MonitorModule {};