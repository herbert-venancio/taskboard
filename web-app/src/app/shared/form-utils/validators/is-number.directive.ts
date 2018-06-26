import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive} from '@angular/core';
import * as _ from 'underscore';

@Directive({
    selector: '[tbIsNumber]',
    providers: [{provide: NG_VALIDATORS, useExisting: IsNumberDirective, multi: true}]
})
export class IsNumberDirective implements Validator {

    validate(control: AbstractControl): ValidationErrors | null {
        const value = control.value;
        const valid = value == null || value === '' || _.isNumber(value);

        return valid ? null : {'isNumber': true};
    }
}
