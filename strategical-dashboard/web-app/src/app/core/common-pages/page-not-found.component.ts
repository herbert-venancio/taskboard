import {Component, Inject, OnInit, Renderer2} from '@angular/core';
import {DOCUMENT} from '@angular/common';

@Component({
    selector: 'sdb-page-not-found',
    templateUrl: './page-not-found.component.html'
})
export class PageNotFoundComponent implements OnInit {
    appIsReady = false;

    constructor(
        private renderer: Renderer2,
        @Inject(DOCUMENT) private document: Document) {
    }

    ngOnInit(): void {
        this.appIsReady = true;
        this.removeAppLoader();
    }

    private removeAppLoader() {
        this.renderer.removeClass(this.document.body, 'app-loading');
    }
}
