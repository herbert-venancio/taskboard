import {Component, Input} from '@angular/core';

/**
 * A circular button with an icon.
 *
 * ### Content
 * The content of this component should be an `<obj-icon>` element.
 */
@Component({
    selector: 'button[obj-icon-button]',
    template: `<ng-content select="obj-icon"></ng-content>`,
    styleUrls: ['./icon-button.component.scss']
})
export class IconButtonComponent {

}
