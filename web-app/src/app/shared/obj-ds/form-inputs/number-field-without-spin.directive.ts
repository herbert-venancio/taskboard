import { Directive, OnDestroy, Optional, Self } from '@angular/core';
import { NgControl, NgForm } from '@angular/forms';
import { AbstractFormInput } from './abstract-form-input';

@Directive({
    selector: 'input[objNumberFieldWithoutSpin]',
    exportAs: 'objNumberFieldWithoutSpin',
    host: {
        'class': 'obj-number-field-without-spin'
    }
})
export class NumberFieldWithoutSpinDirective extends AbstractFormInput implements OnDestroy {
    constructor(@Optional() @Self() ngControl: NgControl, @Optional() ngForm: NgForm) {
        super(ngControl, ngForm, 'objNumberFieldWithoutSpin');
    }
}
