import { Component, ElementRef, Input, Inject } from '@angular/core';
import { ModalComponent } from 'app/shared/obj-ds/modal/modal.component';

/**
 * Extends base ModalComponent with header and footer for buttons
 */
@Component({
    selector: 'tb-modal',
    templateUrl: './tb-modal.component.html',
    styleUrls: ['./tb-modal.component.scss', '../../obj-ds/modal/modal.component.scss']
})
export class TbModalComponent extends ModalComponent {

    @Input('modal-title')
    title: string;

    @Input('modal-icon')
    modalIcon: string;

    constructor(@Inject(ElementRef) el: ElementRef) {
        super(el);
    }
}
