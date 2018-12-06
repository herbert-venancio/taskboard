import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive, Input} from '@angular/core';
import * as moment from 'moment';

@Directive({
    selector: '[tbSameOrBefore]',
    providers: [{provide: NG_VALIDATORS, useExisting: SameOrBeforeDirective, multi: true}]
})
export class SameOrBeforeDirective implements Validator {

    @Input('tbSameOrBefore')
    otherValue: moment.Moment;

    validate(control: AbstractControl): ValidationErrors | null {
        const value = control.value as moment.Moment;
        const valid = value == null || value.isSameOrBefore(this.otherValue);

        return valid ? null : {'sameOrBefore': true};
    }
}
