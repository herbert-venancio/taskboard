import {Component, HostListener} from '@angular/core';

@Component({
    selector: 'obj-menu',
    templateUrl: './menu.component.html',
    styleUrls: ['./menu.component.scss']
})
export class MenuComponent {
    open = false;

    openMenu() {
        this.open = true;
    }

    closeMenu() {
        this.open = false;
    }

    toggleMenu() {
        if (this.open) {
            this.closeMenu();
        } else {
            this.openMenu();
        }
    }

    // TODO: Create a full-screen backdrop behind the menu that will listen for clicks and close the menu.
    @HostListener('document:click')
    public onDocumentClick() {
        this.closeMenu();
    }
}
