import { Component, Input, HostBinding } from '@angular/core';

@Component({
    selector: 'obj-expansion-panel',
    templateUrl: './expansion-panel.component.html',
    styleUrls: ['./expansion-panel.component.scss']
})
export class ExpansionPanelComponent {
    @Input() title: string;

    @HostBinding('class.collapsed')
    collapsed = false;

    toggleCollapsed() {
        this.collapsed = !this.collapsed;
    }

    open() {
        this.collapsed = false;
    }
}
