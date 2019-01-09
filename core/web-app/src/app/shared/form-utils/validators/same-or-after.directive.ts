import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive, Input} from '@angular/core';
import * as moment from 'moment';

@Directive({
    selector: '[tbSameOrAfter]',
    providers: [{provide: NG_VALIDATORS, useExisting: SameOrAfterDirective, multi: true}]
})
export class SameOrAfterDirective implements Validator {

    @Input('tbSameOrAfter')
    otherValue: any;

    validate(control: AbstractControl): ValidationErrors | null {
        const value = control.value as moment.Moment;
        const valid = value == null || this.otherValue == undefined || value.isSameOrAfter(this.otherValue);
        return valid ? null : {'sameOrAfter': true};
    }
}
