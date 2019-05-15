import { Component, Input } from '@angular/core';
import { MonitorData } from '../../project/project/strategical-project-data-set.model';

@Component({
    selector: 'sdb-monitor-item',
    templateUrl: './monitor-item.component.html',
    styleUrls: ['./monitor-item.component.scss']
})

export class MonitorItemComponent {
    @Input() monitor: MonitorData;

    constructor() { }
}

