import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {retry} from 'rxjs/operators';
import {StrategicalProjectDataSet} from './strategical-project-data-set.model';
import { Utils } from '../../../../core/utils/utils';


@Injectable({
    providedIn: 'root'
})
export class ProjectService {

    constructor(private http: HttpClient, private utils: Utils) {}

    getProjects(): Observable<StrategicalProjectDataSet[]> {
        return this.http.get<StrategicalProjectDataSet[]>(`/ws/strategical-dashboard/projects?timezone=${this.utils.getTimeZoneIdFromBrowser()}`)
            .pipe(retry(3));
    }

}
