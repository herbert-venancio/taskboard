import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {retry} from "rxjs/operators";

@Injectable({
    providedIn: 'root'
})
export class ProjectConfigService {

    constructor(private http: HttpClient) {}

    getName(projectKey: string): Observable<string> {
        return this.http.get(`/ws/project/config/${projectKey}/name`, {responseType: 'text'})
            .pipe(retry(3));
    }

}
