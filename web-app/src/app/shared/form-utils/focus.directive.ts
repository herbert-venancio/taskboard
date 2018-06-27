import {Directive, ElementRef, Input, Renderer2} from '@angular/core';

@Directive({
    selector: '[tbFocus]'
})
export class FocusDirective {

    constructor(private elementRef: ElementRef) {}

    @Input('tbFocus')
    set isFocused(focused: boolean) {
        if (focused) {
            this.elementRef.nativeElement.focus();
        }
    }

}
