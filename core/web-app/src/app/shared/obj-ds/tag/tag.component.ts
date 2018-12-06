import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'obj-tag',
    templateUrl: './tag.component.html',
    styleUrls: ['./tag.component.scss']
})
export class TagComponent implements OnInit {

    @Input() text: string;
    @Input() type: string;

    constructor() { }

    ngOnInit() {
        if (this.text === '' || this.text === null || this.text === undefined)
            throw '"text" is required';
    }

}
