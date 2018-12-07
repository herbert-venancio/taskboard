import { Component, HostBinding, Input } from '@angular/core';

/**
 * Extends base ModalComponent with header and footer for buttons
 */
@Component({
    selector: 'obj-modal',
    templateUrl: './obj-modal.component.html',
    styleUrls: ['./obj-modal.component.scss']
})
export class ModalComponent {

    private static modalsOpened: number = 0;

    @HostBinding('hidden')
    private hidden: boolean = true;

    @Input('close-on-backdrop')
    public closeOnBackdrop: boolean = true;

    @Input('modal-title')
    title: string;

    @Input('modal-icon')
    modalIcon: string;

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

    doCloseOnBackdrop() {
        if (this.closeOnBackdrop)
            this.close();
    }
}
