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
    @Input() errors: string;
    @Input() details: string;

    constructor() {}
    
    getErrors(errors: string) {
        let content: string = "";
        const errorArray = errors.split(",");

        if (errors.length > 1) {
            errorArray.map((error: any) => {
                content += "" + error + "\n \n";
            });
        }

        return content;
    }
}