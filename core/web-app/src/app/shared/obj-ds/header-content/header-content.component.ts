import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'obj-header-content',
    template: `
        <obj-title [title]="title"></obj-title>

    `,
    styleUrls: ['./header-content.component.scss']
})
export class HeaderContentComponent implements OnInit {

    @Input() title: string;

    constructor() { }

    ngOnInit() {
    }

}
