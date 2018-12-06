import {Directive, OnDestroy, Optional, Self} from '@angular/core';
import {NgControl, NgForm} from '@angular/forms';
import {AbstractFormInput} from './abstract-form-input';

@Directive({
    selector: 'input[objTextField]',
    exportAs: 'objTextField',
    host: {
        'class': 'obj-text-field'
    }
})
export class TextFieldDirective extends AbstractFormInput implements OnDestroy {
    constructor(@Optional() @Self() ngControl: NgControl, @Optional() ngForm: NgForm) {
        super(ngControl, ngForm, 'objTextField');
    }
}
