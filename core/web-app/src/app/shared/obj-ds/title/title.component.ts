import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'obj-title',
  template: `{{ title }}`,
  styleUrls: ['./title.component.scss']
})
export class TitleComponent implements OnInit {

    @Input() title: string;

    constructor() { }

    ngOnInit() {
        if (this.title === null || this.title === undefined || this.title.trim().length === 0)
            throw new Error('Attribute "title" is required.');
    }

}
