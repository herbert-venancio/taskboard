import {AfterViewInit, Directive, Host, OnDestroy, Optional, Renderer2, Self} from '@angular/core';
import {NgControl, NgForm, RequiredValidator} from '@angular/forms';
import {AbstractFormInput} from '../abstract-form-input';
import {NgSelectComponent} from '@ng-select/ng-select';
import {NotEmptyDirective} from '../../../form-utils/validators/not-empty.directive';

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
    private unsubscribeWindowMouseWheel: Function;

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

        if (!this.ngSelectElement.hasAttribute('appendTo'))
            this.ngSelect.appendTo = 'body';

    }

    ngAfterViewInit() {
        this.closeOnSecondClick();
        this.fixDropdownPosition();
        this.closeOnMouseWheel();
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
            this.unsubscribeCloseOnMouseWheel();
        });
    }

    private closeOnMouseWheel() {
        this.ngSelect.openEvent.subscribe(() => {
            this.subscribeCloseOnMouseWheel();
        });
        this.ngSelect.closeEvent.subscribe(() => {
            this.unsubscribeCloseOnMouseWheel();
        });
    }

    private subscribeCloseOnMouseWheel(): void {
        if (this.unsubscribeWindowMouseWheel)
            return;

        this.unsubscribeWindowMouseWheel = this.renderer.listen('window', 'mousewheel', (event) => {
            if (this.ngSelect.dropdownPanel) {
                const isScrollOnDropdown = this.ngSelect.dropdownPanel.scrollElementRef.nativeElement.contains(event.target);
                if (!isScrollOnDropdown)
                    this.ngSelect.close();
            }
        });
    }

    private unsubscribeCloseOnMouseWheel(): void {
        if (this.unsubscribeWindowMouseWheel) {
            this.unsubscribeWindowMouseWheel();
            this.unsubscribeWindowMouseWheel = undefined;
        }
    }

    private fixDropdownPosition(): void {
        if (this.ngSelect.appendTo) {
            this.ngSelectElement.classList.add('ng-select-outside-dropdown');
            this.ngSelect.openEvent.subscribe(() => {
                this.ngSelect.detectChanges();
                setTimeout(() => {
                    this.ngSelect.dropdownPanel.scrollElementRef.nativeElement.classList.add('animation');
                    this.ngSelect.updateDropdownPosition();
                }, 5);
            });
        }
    }

    ngOnDestroy() {
        this.unsubscribeInputContainerMouseDown();
        this.unsubscribeCloseOnMouseWheel();
        this.ngSelect.closeEvent.unsubscribe();
        this.ngSelect.openEvent.unsubscribe();
        super.ngOnDestroy();
    }

}
