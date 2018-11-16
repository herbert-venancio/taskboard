import { ClusterAlgorithmExecution, ClusterAlgorithmRequest, ClusterGrouping, ClusteringType, DateRange } from './cluster-algorithm-model';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable, of } from 'rxjs';
import { map, flatMap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class ClusterAlgorithmService {

    constructor(private http: HttpClient) {}

    fetchDefaults(): Observable<ClusterAlgorithmRequest> {
        return this.http.get<ClusterAlgorithmRequest>(`/ws/cluster/algorithm/form/defaults`);
    }

    executeAlgorithm(request: ClusterAlgorithmRequest): Observable<ClusterAlgorithmExecution>;
    executeAlgorithm(projects: string[], grouping: ClusterGrouping, clustering: ClusteringType, dateRange: DateRange): Observable<ClusterAlgorithmExecution>;
    executeAlgorithm(arg0: ClusterAlgorithmRequest | string[], arg1?: ClusterGrouping, arg2?: ClusteringType, arg3?: DateRange): Observable<ClusterAlgorithmExecution> {
        let request$: Observable<ClusterAlgorithmRequest>;
        if (arg0 instanceof ClusterAlgorithmRequest) {
            request$ = of(arg0);
        } else {
            const projects: string[] = arg0;
            const grouping: ClusterGrouping = arg1;
            const clustering: ClusteringType = arg2;
            const dateRange: DateRange = arg3;
            request$ = this.fetchDefaults()
            .pipe(
                map(defaults => {
                    defaults.projects = projects;
                    defaults.dateRange = dateRange;
                    defaults.clusterGrouping = grouping;
                    defaults.clusteringType = clustering;
                    return defaults;
                })
            );
        }
        return request$.pipe(
            flatMap(request => this.http.post<ClusterAlgorithmExecution>(`/ws/cluster/algorithm`, request))
        );
    }

    fetchResult(executionId: number): Observable<ClusterAlgorithmExecution> {
        return this.http.get<ClusterAlgorithmExecution>(`/ws/cluster/algorithm/${executionId}`);
    }
}
