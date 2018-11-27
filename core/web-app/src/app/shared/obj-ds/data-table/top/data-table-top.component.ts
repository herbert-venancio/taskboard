import {Component, Input} from '@angular/core';

@Component({
  selector: 'obj-data-table-top',
  template: `
      <div class="title">{{ title }}</div>
      <div class="actions">
          <ng-content></ng-content>
      </div>
  `,
  styleUrls: ['./data-table-top.component.scss']
})
export class DataTableTopComponent {

    @Input() title: string;

    constructor() { }

}
