import {Component, Inject, OnInit, Renderer2} from '@angular/core';
import {InitialDataService} from './core/initial-data.service';
import {DOCUMENT} from '@angular/common';
import * as moment from 'moment';

@Component({
    selector: 'tb-app',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    appIsReady = false;

    constructor(
        private initialDataService: InitialDataService,
        private renderer: Renderer2,
        @Inject(DOCUMENT) private document: Document) {

        moment.locale(window.navigator.language);
    }

    ngOnInit(): void {
        this.initialDataService.load(() => {
            this.appIsReady = true;
            this.removeAppLoader();
        });
    }

    private removeAppLoader() {
        this.renderer.removeClass(this.document.body, 'app-loading');
    }
}
