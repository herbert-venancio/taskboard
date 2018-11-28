import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {retry} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ProjectChangeRequestsService {

    constructor(private http: HttpClient) {}

    getData(projectKey: string): Observable<ProjectChangeRequestDto[]> {
        return this.http.get<ProjectChangeRequestDto[]>(`/ws/project/config/change-request/${projectKey}`)
            .pipe(retry(3));
    }

    updateItems(projectKey: string, items: ProjectChangeRequestDto[]): Observable<any> {
        return this.http.put(`/ws/project/config/change-request/${projectKey}`, items);
    }
}

export class ProjectChangeRequestDto {
    id: number;
    name: string;
    date: string;
    budgetIncrease: number;
    isBaseline: boolean;
}