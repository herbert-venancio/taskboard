import { Component, Input } from '@angular/core';

@Component({
    selector: 'obj-toolbar',
    templateUrl: './obj-toolbar.component.html',
    styleUrls: [ './obj-toolbar.component.scss' ]
})
export class ToolbarComponent {

    @Input() title: string;
    @Input() subtitle: string;

    get hasSubtitle() {
        return this.subtitle && this.subtitle.length > 0;
    }
}
