import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {retry} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ProjectProfileService {

    constructor(private http: HttpClient) {}

    getData(projectKey: string): Observable<ProjectProfileDataDto> {
        return this.http.get<ProjectProfileDataDto>(`/ws/project/config/project-profile/${projectKey}/data`)
            .pipe(retry(3));
    }

    updateItems(projectKey: string, items: ProjectProfileItemDto[]): Observable<any> {
        return this.http.put(`/ws/project/config/project-profile/${projectKey}/items`, items);
    }
}

export class ProjectProfileDataDto {
    projectName: string;
    items: ProjectProfileItemDto[];
}

export class ProjectProfileItemDto {
    id: number;
    roleName: string;
    peopleCount: number;
    allocationStart: string;
    allocationEnd: string;
}
