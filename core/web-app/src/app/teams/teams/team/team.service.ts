import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {retry} from 'rxjs/operators';
import {TeamDto} from '../team-dto.model';

@Injectable({
    providedIn: 'root'
})
export class TeamService {

    constructor(private http: HttpClient) {}

    getTeam(teamName: string): Observable<TeamDto> {
        return this.http.get<TeamDto>(`/ws/teams/${teamName}`)
            .pipe(retry(3));
    }

    updateTeam(team: TeamDto): Observable<any> {
        return this.http.put(`/ws/teams/${team.name}`, team);
    }

    getUsersWith(nameWith: string): Observable<string[]> {
        return this.http.get<string[]>(`/ws/users/search?onlyNames=true&query=${nameWith}`)
            .pipe(retry(3));
    }

}
