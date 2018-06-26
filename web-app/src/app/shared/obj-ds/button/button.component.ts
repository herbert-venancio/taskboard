import {Component} from '@angular/core';

/**
 * A regular button with background.
 *
 * ### Models
 * Use one of the following style classes to specify the button model: `primary` (blue), `secondary` (white) or `input-action` (orange).
 * Default model: secondary.
 */
@Component({
    selector: 'button[obj-button]',
    template: `<ng-content></ng-content>`,
    styleUrls: ['./button.component.scss']
})
export class ButtonComponent {
}
