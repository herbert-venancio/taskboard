import { Component } from '@angular/core';

@Component({
    selector: 'obj-modal-footer-left',
    template: `<ng-content></ng-content>`,
})
export class ModalFooterLeftComponent {

}

@Component({
    selector: 'obj-modal-footer-right',
    template: `<ng-content select="button[obj-button]"></ng-content>`,
    styleUrls: ['./obj-modal-footer.component.scss']
})
export class ModalFooterRightComponent {

}
