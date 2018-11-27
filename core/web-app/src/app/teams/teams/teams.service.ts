import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {retry} from 'rxjs/operators';
import {TeamDto} from './team-dto.model';

@Injectable({
    providedIn: 'root'
})
export class TeamsService {

    constructor(private http: HttpClient) {}

    getTeams(): Observable<TeamDto[]> {
        return this.http.get<TeamDto[]>(`/ws/teams`)
            .pipe(retry(3));
    }

}
