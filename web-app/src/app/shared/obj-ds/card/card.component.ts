import {Component, HostBinding, Input, ViewEncapsulation} from '@angular/core';

/**
 * An entry point for detailed information.
 *
 * ### Content
 * The content of this component should be a combination of the following components:
 * - `<obj-card-title>` (optional)
 * - `<obj-card-actions>` (optional)
 * - `<obj-card-content>` (required)
 */
@Component({
    selector: 'obj-card',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.scss']
})
export class CardComponent {
    @Input()
    @HostBinding('class.collapsible')
    collapsible = false;

    @HostBinding('class.collapsed')
    collapsed = false;

    toggleCollapsed() {
        this.collapsed = !this.collapsed;
    }
}
