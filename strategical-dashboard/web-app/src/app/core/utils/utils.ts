import {Injectable} from '@angular/core';
import * as _ from 'underscore';
import * as moment from 'moment';
import * as jstz from 'jstz';

@Injectable({
    providedIn: 'root'
})
export abstract class Utils {
    constructor() { }

    getTimeZoneIdFromBrowser = () => {
        return Intl.DateTimeFormat().resolvedOptions().timeZone;
    }
}
