import {Directive, ElementRef, Input, Renderer2, Host, Optional} from '@angular/core';
import {NgSelectComponent} from '@ng-select/ng-select';

@Directive({
    selector: '[tbFocus]'
})
export class FocusDirective {

    constructor(private elementRef: ElementRef,
        @Host() @Optional() private ngSelectComponent: NgSelectComponent) {}

    @Input('tbFocus')
    set isFocused(focused: boolean) {
        if (focused) {
            if(this.ngSelectComponent !== null)
                this.ngSelectComponent.focus();
            else
                this.elementRef.nativeElement.focus();
        }
    }

}
