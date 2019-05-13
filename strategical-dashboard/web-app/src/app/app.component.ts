import { Component, OnInit } from '@angular/core';
import {InitialDataService} from './core/initial-data.service';
import * as moment from 'moment';

@Component({
    selector: 'sdb-app',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})

export class AppComponent implements OnInit {
    appIsReady = false;

    constructor(
        private initialDataService: InitialDataService) {
        moment.locale(window.navigator.language);
    }

    ngOnInit(): void {
        this.initialDataService.load(() => {
            this.appIsReady = true;
        });
    }
}