import { ClusterAlgorithmExecution, ClusterAlgorithmRequest, ClusterGrouping, ClusteringType, DateRange } from './cluster-algorithm.model';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { map, flatMap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class ClusterAlgorithmService {

    constructor(private http: HttpClient) {}

    fetchDefaults(): Observable<ClusterAlgorithmRequest> {
        return this.http.get<ClusterAlgorithmRequest>(`/ws/cluster/algorithm/form/defaults`);
    }

    executeAlgorithm(
        projects: string[],
        grouping: ClusterGrouping,
        clustering: ClusteringType,
        dateRange: DateRange
    ): Observable<ClusterAlgorithmExecution> {
        return this.fetchDefaults()
            .pipe(
                map(defaults => {
                    defaults.projects = projects;
                    defaults.dateRange = dateRange;
                    defaults.clusterGrouping = grouping;
                    defaults.clusteringType = clustering;
                    return defaults;
                }),
                flatMap(request => this.http.post<ClusterAlgorithmExecution>(`/ws/cluster/algorithm`, request))
            );
    }

    fetchResult(executionId: number): Observable<ClusterAlgorithmExecution> {
        return this.http.get<ClusterAlgorithmExecution>(`/ws/cluster/algorithm/${executionId}`);
    }
}
