import { Component, Input } from '@angular/core';
import { StrategicalProjectDataSet } from './strategical-project-data-set.model';

@Component({
    selector: 'sdb-project-item',
    templateUrl: 'project-item.component.html',
    styleUrls: ['project-item.component.scss']
})
export class ProjectItemComponent {
    @Input() project: StrategicalProjectDataSet;
    constructor() { }
}
