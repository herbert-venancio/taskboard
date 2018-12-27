import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/internal/Observable';
import { retry } from 'rxjs/operators';
import { BaseClusterDto } from './base-cluster/base-cluster-dto.model';

@Injectable({
    providedIn: 'root'
})
export class BaseClusterService {

    private baseClusterUrl = '/ws/base-cluster';

    constructor(private http: HttpClient) { }

    findOne(clusterId: number): Observable<BaseClusterDto> {
        const findURI = this.baseClusterUrl + '/' + clusterId;

        return this.http.get<BaseClusterDto>(findURI)
            .pipe(retry(3));
    }

    findAll(): Observable<BaseClusterDto[]> {
        return this.http.get<BaseClusterDto[]>(this.baseClusterUrl)
            .pipe(retry(3));
    }

    update(baseClusterId: number, baseClusterDto: BaseClusterDto): Observable<void> {
        const updateURI = this.baseClusterUrl + '/' + baseClusterId;

        return this.http.put<any>(updateURI, baseClusterDto);
    }

    create(baseClusterDto: BaseClusterDto): Observable<any> {
        return this.http.post<any>(this.baseClusterUrl, baseClusterDto, { observe: 'response' });
    }

    getNewModel(): Observable<BaseClusterDto> {
        const creationURI = this.baseClusterUrl + '/new';

        return this.http.get<BaseClusterDto>(creationURI)
            .pipe(retry(3));
    }

    getAuthorizedProjectNames(): Observable<string[]> {
        const authorizedProjectNamesURI = this.baseClusterUrl + '/projects';

        return this.http.get<string[]>(authorizedProjectNamesURI)
            .pipe(retry(3));
    }

}
