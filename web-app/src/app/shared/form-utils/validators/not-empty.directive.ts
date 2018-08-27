import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive} from '@angular/core';

@Directive({
    selector: '[tbNotEmpty]',
    providers: [{provide: NG_VALIDATORS, useExisting: NotEmptyDirective, multi: true}]
})
export class NotEmptyDirective implements Validator {

    validate(c: AbstractControl): ValidationErrors | null {
        return this.isEmpty(c.value) ? {'notEmpty': true} : null;
    }

    private isEmpty(value: any): boolean {
        if (value === null || value === undefined)
            return true;
        return typeof value === 'string' && value.trim().length === 0;
    }

}
