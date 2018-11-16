import { Component } from '@angular/core';

@Component({
    selector: 'tb-modal-footer-left',
    template: `<ng-content></ng-content>`,
})
export class TbModalFooterLeftComponent {

}

@Component({
    selector: 'tb-modal-footer-right',
    template: `<ng-content select="button[obj-button]"></ng-content>`,
    styleUrls: ['./tb-modal-footer.component.scss']
})
export class TbModalFooterRightComponent {

}
