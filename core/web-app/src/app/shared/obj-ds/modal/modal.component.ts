import { Component, ElementRef, HostBinding, Input } from '@angular/core';

/**
 * Basic modal with just open/close functionality
 */
@Component({
    selector: 'obj-modal',
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.scss']
})
export class ModalComponent {

    private static modalsOpened: number = 0;

    private element: any;

    @HostBinding('hidden')
    private hidden: boolean = true;

    @Input('easy-close')
    public easyClose: boolean = true;

    constructor(el: ElementRef) {
        this.element = el.nativeElement;
    }

    get isOpen() {
        return !this.hidden;
    }

    set isOpen(value: boolean) {
        if (value)
            this.open();
        else
            this.close();
    }

    open(): void {
        if (this.hidden) {
            ModalComponent.modalsOpened++;
            if (ModalComponent.modalsOpened === 1)
                document.body.classList.add('modal-open');
        }
        this.hidden = false;
    }

    close(): void {
        if (!this.hidden) {
            ModalComponent.modalsOpened--;
            if (ModalComponent.modalsOpened === 0)
                document.body.classList.remove('modal-open');
        }
        this.hidden = true;
    }

    doEasyClose() {
        if (this.easyClose)
            this.close();
    }
}
