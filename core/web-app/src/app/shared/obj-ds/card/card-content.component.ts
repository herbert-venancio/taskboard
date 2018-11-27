import {Component, Input} from '@angular/core';

/**
 * Component intended to be used within the `<obj-card>` component.
 */
@Component({
    selector: 'obj-card-content',
    template: '<ng-content></ng-content>'
})
export class CardContentComponent {
}
