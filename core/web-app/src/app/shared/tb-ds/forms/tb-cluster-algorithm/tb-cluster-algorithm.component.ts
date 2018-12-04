import { Component, ViewChild, Input, Inject, Output, EventEmitter, ElementRef } from '@angular/core';
import { Observable, interval, pipe, zip } from 'rxjs';
import { flatMap, concatMap, filter, take, map } from 'rxjs/operators';
import { Moment } from 'moment';
import { NgForm } from '@angular/forms';
import * as _ from 'underscore';

import { SnackbarControl } from 'app/shared/obj-ds/snackbar/snackbar-control';
import { DATE_INPUT_DISPLAY_FORMAT } from 'app/shared/form-utils/date-input.directive';
import { ClusterGrouping, ClusteringType, DateRange, ClusterAlgorithmExecution } from 'app/cluster/cluster-algorithm.model';
import { ClusterAlgorithmService } from 'app/cluster/cluster-algorithm.service';
import { ClusterItemDto } from 'app/shared/tb-ds/forms/tb-cluster/cluster-item-dto.model';

@Component({
    selector: 'tb-cluster-algorithm',
    templateUrl: './tb-cluster-algorithm.component.html',
    styleUrls: ['./tb-cluster-algorithm.component.scss']
})
export class TbClusterAlgorithmComponent {

    snackbar = new SnackbarControl();

    @Input('project')
    singleProject: string;

    @Output('results')
    results = new EventEmitter<ClusterItemDto[]>();
    @Output('submitted')
    submitted = new EventEmitter<boolean>();
    @Output()
    errorEvent = new EventEmitter();

    startDate: Moment;
    endDate: Moment;

    @ViewChild(NgForm) tbClusterAlgorithm: NgForm;
    @ViewChild('startDateField', { read: ElementRef }) startDateField: ElementRef;
    @ViewChild('endDateField', { read: ElementRef }) endDateField: ElementRef;

    private waitFinished = pipe(
        flatMap((job: ClusterAlgorithmExecution) => this.resultPolling(job.executionId)),
        filter(job => job.status === 'finished' || job.status === 'error' || job.status === 'cancelled'),
        take(1)
    );

    constructor(
        private clusterAlgorithmService: ClusterAlgorithmService,
        @Inject(DATE_INPUT_DISPLAY_FORMAT) public dateDisplayFormat: string
    ) {
    }

    submit() {
        (this.tbClusterAlgorithm as any).submitted = true;
        this.tbClusterAlgorithm.ngSubmit.emit();
    }

    runAllClusterings() {
        if (this.tbClusterAlgorithm.invalid) {
            this.focusOnFirstError();
            this.sendInvalidSubmitEvent();
            return;
        }
        this.sendSubmittedEvent();

        const dateRange = this.buildDateRange();

        // ballpark + effort clustering algorithm execution
        const ballparkEffort$ = this.clusterAlgorithmService
        .executeAlgorithm([this.singleProject], ClusterGrouping.BALLPARK, ClusteringType.EFFORT_ONLY, dateRange)
        .pipe(this.waitFinished);
        // ballpark + cycle clustering algorithm execution
        const ballparkCycle$ = this.clusterAlgorithmService
        .executeAlgorithm([this.singleProject], ClusterGrouping.BALLPARK, ClusteringType.CYCLE_ONLY, dateRange)
        .pipe(this.waitFinished);
        // subtask + effort clustering algorithm execution
        const subtaskEffort$ = this.clusterAlgorithmService
        .executeAlgorithm([this.singleProject], ClusterGrouping.SUBTASK, ClusteringType.EFFORT_ONLY, dateRange)
        .pipe(this.waitFinished);
        // subtask + cycle clustering algorithm execution
        const subtaskCycle$ = this.clusterAlgorithmService
        .executeAlgorithm([this.singleProject], ClusterGrouping.SUBTASK, ClusteringType.CYCLE_ONLY, dateRange)
        .pipe(this.waitFinished);

        zip(ballparkEffort$, ballparkCycle$, subtaskEffort$, subtaskCycle$)
        .pipe(
            map(results => {
                const ballparkEffort = results[0].result;
                const ballparkCycle = results[1].result;
                const subtaskEffort = results[2].result;
                const subtaskCycle = results[3].result;

                // merge all {key:value}s into a single object
                const effort = _.extend({}, ballparkEffort, subtaskEffort);
                const cycle = _.extend({}, ballparkCycle, subtaskCycle);

                const clusterItems: ClusterItemDto[] = [];
                Object.keys(effort).forEach(issueType => {
                    Object.keys(effort[issueType].clusters).forEach(tsize => {
                        clusterItems.push(new ClusterItemDto(
                            // convert days to hours
                            cycle[issueType].clusters[tsize].centroid.values[0] * 8
                            , effort[issueType].clusters[tsize].centroid.values[0]
                            , false
                            , issueType
                            , tsize
                        ));
                    });
                });

                return clusterItems;
            })
        )
        .subscribe(
            executions => this.results.emit(executions),
            error => this.errorEvent.emit(error)
        );
    }

    buildDateRange(): DateRange {
        const startDate = this.startDate ? this.startDate.format('YYYY-MM-DD') : null;
        const endDate = this.endDate ? this.endDate.format('YYYY-MM-DD') : null;
        return new DateRange(startDate, endDate);
    }

    private resultPolling(executionId: number): Observable<ClusterAlgorithmExecution> {
        const result$ = this.clusterAlgorithmService.fetchResult(executionId);
        return interval(250)
        .pipe(
            concatMap(() => result$)
        );
    }

    private focusOnFirstError() {
        if (this.tbClusterAlgorithm.control.get('startDate').invalid)
            this.startDateField.nativeElement.focus();
        if (this.tbClusterAlgorithm.control.get('endDate').invalid)
            this.endDateField.nativeElement.focus();
    }

    private sendSubmittedEvent() {
        this.submitted.emit(true);
    }

    private sendInvalidSubmitEvent() {
        this.submitted.emit(false);
    }
}
