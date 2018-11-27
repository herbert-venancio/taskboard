import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class ProjectClusterService {

    constructor(private http: HttpClient) {}

    get(projectKey: string): Observable<ProjectClusterItemDto[]> {
        return this.http.get<ProjectClusterItemDto[]>(`/ws/project/config/cluster/${projectKey}`)
            .pipe(retry(3));
    }

    update(projectKey: string, items: ProjectClusterItemDto[]): Observable<any> {
        return this.http.put(`/ws/project/config/cluster/${projectKey}`, items);
    }
}

export class ProjectClusterItemDto {
    projectKey: string;
    issueType: string;
    sizing: string;
    effort: number;
    cycle: number;
    fromBaseCluster: boolean;
}
