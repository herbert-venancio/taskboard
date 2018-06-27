import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'obj-avatar',
    templateUrl: './avatar.component.html',
    styleUrls: ['./avatar.component.scss']
})
export class AvatarComponent implements OnInit {
    @Input() name: string;
    @Input() imageUrl?: string;

    imageLoaded = false;

    ngOnInit() {
    }

    getInitials(): string {
        return this.name.split(' ').slice(0, 2).map(v => v.charAt(0)).join('');
    }

    onImageLoad() {
        this.imageLoaded = true;
    }
}
