import {Directive, HostListener, Self} from '@angular/core';
import {NgForm} from '@angular/forms';

@Directive({
    selector: 'form[tbLeaveConfirmation]'
})
export class LeaveConfirmationDirective {

    constructor(@Self() private form: NgForm) {}

    @HostListener('window:beforeunload', ['$event'])
    beforeUnload($event: Event): string | undefined {
        if (this.form.pristine) {
            return undefined;
        }

        $event.returnValue = true;
        return 'If you leave before saving, your changes will be lost.';
    }

}
