import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive, Input} from '@angular/core';
import * as _ from 'underscore';

@Directive({
    selector: '[tbMin]',
    providers: [{provide: NG_VALIDATORS, useExisting: MinDirective, multi: true}]
})
export class MinDirective implements Validator {

    @Input('tbMin')
    minValue: number;

    validate(control: AbstractControl): ValidationErrors | null {
        const value = control.value;
        const valid = value == null || value === '' || !_.isNumber(value) || value >= this.minValue;

        return valid ? null : {'min': true};
    }
}
