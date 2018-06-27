import {Component, Input} from '@angular/core';

@Component({
  selector: 'obj-error-message',
  template: '<ng-content *ngIf="isVisible"></ng-content>'
})
export class ErrorMessageComponent {
    private messageErrorCodes: string[];

    isVisible = false;

    /**
     * The error code that this message represents. Comma-separated values are supported.
     * E.g. `required`.
     */
    @Input()
    set error(error: string) {
        this.messageErrorCodes = error.split(',').map(e => e.trim());
    }

    match(errorCodes: string[]): boolean {
        for (let i = 0, len = this.messageErrorCodes.length; i < len; i++) {
            const error = this.messageErrorCodes[i];

            if (errorCodes.indexOf(error) > -1) {
                return true;
            }
        }

        return false;
    }
}
