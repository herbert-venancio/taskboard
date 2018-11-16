import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';
import { ClusterItemDto } from 'app/shared/tb-ds/forms/tb-cluster/cluster-item-dto.model';

@Injectable({
    providedIn: 'root'
})
export class ProjectClusterService {

    constructor(private http: HttpClient) {}

    get(projectKey: string): Observable<ClusterItemDto[]> {
        return this.http.get<ClusterItemDto[]>(`/ws/project/config/cluster/${projectKey}`)
            .pipe(retry(3));
    }

    update(projectKey: string, items: ClusterItemDto[]): Observable<any> {
        return this.http.put(`/ws/project/config/cluster/${projectKey}`, items);
    }
}
