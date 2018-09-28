import {Component, ElementRef, forwardRef, HostBinding, HostListener, Input, ViewChild} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
    selector: 'obj-search',
    templateUrl: './search-field.component.html',
    host: {
        'class': 'obj-search-field'
    },
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SearchFieldComponent),
            multi: true
        }
    ]
})
export class SearchFieldComponent implements ControlValueAccessor {

    @Input() placeholder: string = '';
    @Input() disabled = false;

    @ViewChild('searchInput') searchInput: ElementRef;

    searchText: string;

    @HostBinding('class.obj-search-focused')
    focused = false;

    private keepFocusStyleOnBlur: boolean = false;

    @HostListener('mousedown', ['$event'])
    onHostMouseDown() {
        this.keepFocusStyleOnBlur = true;
        this.focused = true;
    }

    @HostListener('mouseup', ['$event'])
    onHostMouseUp() {
        this.keepFocusStyleOnBlur = false;
        this.searchInput.nativeElement.focus();
    }

    @HostListener('focusout', ['$event'])
    onHostBlur() {
        if(this.keepFocusStyleOnBlur)
            return;

        this.focused = false;
        this.onTouched();
    }

    clear() {
        this.writeValue('');
    }

    private onChange(value: string) {}
    private onTouched() {}

    registerOnChange(fn: (value: string) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    writeValue(value: string): void {
        this.searchText = value;
        if (this.focused)
            this.onChange(value);
    }

    showClear(value: string): boolean {
        return value !== undefined && value !== null && value !== '';
    }

}
