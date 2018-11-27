import {Directive, HostListener, Input} from '@angular/core';
import {MenuComponent} from './menu.component';

@Directive({
    selector: '[objMenuToggle]'
})
export class MenuToggleDirective {
    @Input('objMenuToggle') menu: MenuComponent;

    @HostListener('click', ['$event'])
    onClick(event: any) {
        this.menu.toggleMenu();
        event.stopPropagation();
    }
}
