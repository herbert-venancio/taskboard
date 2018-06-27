import {Component} from '@angular/core';

/**
 * A flat button without background.
 *
 * ### Content
 * The content of this component should be a text or an `<obj-icon>` element followed by text.
 *
 * ### Models
 * Use one of the following style classes to specify the button model: `primary` (blue) or `secondary` (orange).
 * Default model: primary.
 */
@Component({
    selector: 'button[obj-flat-button]',
    template: `<ng-content></ng-content>`,
    styleUrls: ['./flat-button.component.scss']
})
export class FlatButtonComponent {
}
