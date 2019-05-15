import { Component, Input } from '@angular/core';

@Component({
    selector: 'sdb-monitor-cost',
    templateUrl: './monitor-cost.component.html',
    styleUrls: ['./monitor-cost.component.scss']
})

export class MonitorCostComponent {
    @Input() range: string;
    @Input() label: string;
    @Input() status: string = 'normal';
    @Input() details: string;

    constructor() {}
}
