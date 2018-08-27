import {AfterViewInit, Directive, Host, OnDestroy, Optional, Renderer2, Self} from '@angular/core';
import {NgControl, NgForm, RequiredValidator} from '@angular/forms';
import {AbstractFormInput} from '../abstract-form-input';
import {NgSelectComponent} from '@ng-select/ng-select';
import {NotEmptyDirective} from "../../../form-utils/validators/not-empty.directive";

@Directive({
    selector: 'ng-select',
    exportAs: 'objSelectField',
    host: {
        'class': 'obj-select-field'
    }
})
export class SelectFieldDirective extends AbstractFormInput implements AfterViewInit, OnDestroy {

    private isOpen = false;
    private unsubscribeInputContainerMouseDown: Function;
    private ngSelectElement: Element;

    constructor(@Optional() @Self() ngControl: NgControl,
                @Optional() ngForm: NgForm,
                @Host() @Self() private ngSelect: NgSelectComponent,
                @Host() @Optional() private notEmpty: NotEmptyDirective,
                @Host() @Optional() private isRequired: RequiredValidator,
                private renderer: Renderer2) {
        super(ngControl, ngForm, 'objSelectField');
        this.setDefaultValues();
    }

    private setDefaultValues(): void {
        this.ngSelectElement = this.ngSelect.elementRef.nativeElement;

        if (this.notEmpty !== null || this.isRequired !== null)
            this.ngSelect.clearable = false;

        if (this.ngSelectElement.hasAttribute('readonly')) {
            this.ngSelectElement.setAttribute('disabled', 'disabled');
            this.ngSelect.disabled = true;
        }
    }

    ngAfterViewInit() {
        this.closeOnSecondClick();
    }

    private closeOnSecondClick(): void {
        const inputContainer = this.ngSelectElement.querySelector('.ng-select-container');
        this.unsubscribeInputContainerMouseDown = this.renderer.listen(inputContainer, 'mousedown', () => {
            if (!this.isOpen && this.ngSelect.isOpen)
                this.isOpen = true;
            else
                this.ngSelect.close();
        });
        this.ngSelect.closeEvent.subscribe(() => {
            this.isOpen = false;
        });
    }

    ngOnDestroy() {
        this.unsubscribeInputContainerMouseDown();
        this.ngSelect.closeEvent.unsubscribe();
        super.ngOnDestroy();
    }

}
