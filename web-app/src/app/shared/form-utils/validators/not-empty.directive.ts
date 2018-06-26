import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive} from '@angular/core';

@Directive({
    selector: '[tbNotEmpty]',
    providers: [{provide: NG_VALIDATORS, useExisting: NotEmptyDirective, multi: true}]
})
export class NotEmptyDirective implements Validator {

    validate(c: AbstractControl): ValidationErrors | null {
        const value: string = c.value;
        return value == null || value.trim().length === 0 ? {'notEmpty': true} : null;
    }

}
