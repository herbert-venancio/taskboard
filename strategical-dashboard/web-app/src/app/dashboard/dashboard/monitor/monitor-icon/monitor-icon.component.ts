import { Component, Input } from '@angular/core';

@Component({
    selector: 'sdb-monitor-icon',
    templateUrl: './monitor-icon.component.html',
    styleUrls: ['./monitor-icon.component.scss']
})

export class MonitorIconComponent {
    @Input() icon: string;
    @Input() label: string;
}