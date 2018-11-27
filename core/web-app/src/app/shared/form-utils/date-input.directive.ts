import {
    AbstractControl,
    ControlValueAccessor,
    NG_VALIDATORS,
    NG_VALUE_ACCESSOR,
    ValidationErrors,
    Validator
} from '@angular/forms';
import {Directive, ElementRef, forwardRef, HostBinding, HostListener, InjectionToken} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';

const DISPLAY_FORMAT = 'L';

export const DATE_INPUT_DISPLAY_FORMAT = new InjectionToken<string>('Date Long Display Format', {
    providedIn: 'root',
    factory: () => moment.localeData().longDateFormat(DISPLAY_FORMAT).toLowerCase()
});

@Directive({
    selector: '[tbDateInput]',
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => DateInputDirective),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => DateInputDirective),
            multi: true,
        }
    ]
})
export class DateInputDirective implements ControlValueAccessor, Validator {
    private disabled: boolean;
    private inputIsValid = true;

    private cvaOnChange: (value: any) => void = () => {};
    private cvaOnTouched = () => {};

    constructor(private elementRef: ElementRef) {}

    registerOnChange(fn: any): void {
        this.cvaOnChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.cvaOnTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    writeValue(modelValue: Moment): void {
        this.elementRef.nativeElement.value = modelValue && modelValue.isValid() ? modelValue.format(DISPLAY_FORMAT) : '';
    }

    @HostBinding('disabled')
    get isDisabled(): boolean {
        return this.disabled;
    }

    @HostListener('blur')
    onBlur(): void {
        this.cvaOnTouched();
    }

    @HostListener('input', ['$event.target.value'])
    onInput(inputValue: string): void {
        if (inputValue == null || inputValue === '') {
            this.inputIsValid = true;
            this.cvaOnChange(null);
            return;
        }

        const modelValue = moment(inputValue, DISPLAY_FORMAT, true);

        this.inputIsValid = modelValue.isValid();
        this.cvaOnChange(modelValue.isValid() ? modelValue : null);
    }

    validate(c: AbstractControl): ValidationErrors | null {
        const inputValue = this.elementRef.nativeElement.value;
        return this.inputIsValid ? null : {'dateParse': {'text': inputValue}};
    }
}
