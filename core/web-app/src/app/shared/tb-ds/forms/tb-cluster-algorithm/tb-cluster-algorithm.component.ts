import { Component, ViewChild, Input, Inject, Output, EventEmitter, ElementRef, OnInit } from '@angular/core';
import { Observable, interval, pipe, zip } from 'rxjs';
import { flatMap, concatMap, filter, take, map } from 'rxjs/operators';
import { Moment } from 'moment';
import { NgForm } from '@angular/forms';
import * as _ from 'underscore';

import { DATE_INPUT_DISPLAY_FORMAT } from 'app/shared/form-utils/date-input.directive';
import { ClusterGrouping, ClusteringType, DateRange, ClusterAlgorithmExecution } from 'app/shared/tb-ds/forms/tb-cluster-algorithm/cluster-algorithm.model';
import { ClusterAlgorithmService } from 'app/shared/tb-ds/forms/tb-cluster-algorithm/cluster-algorithm.service';
import { ClusterItemDto } from 'app/shared/tb-ds/forms/tb-cluster/cluster-item-dto.model';
import { CheckboxDto } from '../../../model-utils/checkbox-dto.model';

@Component({
    selector: 'tb-cluster-algorithm',
    templateUrl: './tb-cluster-algorithm.component.html',
    styleUrls: ['./tb-cluster-algorithm.component.scss']
})
export class TbClusterAlgorithmComponent implements OnInit {

    startDate: Moment;
    endDate: Moment;

    selectableProjectsCheckboxes: CheckboxDto[];

    @Input() projects: string[];

    @Input() set selectableProjects(projects: string[]) {
        if (projects)
            this.selectableProjectsCheckboxes = CheckboxDto.fromValues(projects);
        else
            this.selectableProjectsCheckboxes = undefined;
    }

    @Output() results = new EventEmitter<ClusterItemDto[]>();
    @Output() submitted = new EventEmitter<boolean>();
    @Output() errorEvent = new EventEmitter();

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
    ) {}

    ngOnInit() {
        if (this.projects === undefined && this.selectableProjectsCheckboxes === undefined)
            throw new Error(`You should pass "projects" or "selectableProjects" parameters.`);

        else if (this.projects !== undefined && this.selectableProjectsCheckboxes !== undefined)
            throw new Error(`"projects" and "selectableProjects" parameters shouldn't be used together.`);
    }

    submit() {
        (this.tbClusterAlgorithm as any).submitted = true;
        this.tbClusterAlgorithm.ngSubmit.emit();
    }

    getSelectedProject() {
        if (this.projects)
            return this.projects;
        else
            return CheckboxDto.getSelectedValues(this.selectableProjectsCheckboxes);
    }

    doAlgorithmExecution(selectedProjects: string[], grouping: ClusterGrouping, clustering: ClusteringType, dateRange: DateRange) {
        return this.clusterAlgorithmService
            .executeAlgorithm(selectedProjects, grouping, clustering, dateRange)
            .pipe(this.waitFinished);
    }

    runAllClusterings() {
        if (this.tbClusterAlgorithm.invalid) {
            this.focusOnFirstError();
            this.sendInvalidSubmitEvent();
            return;
        }

        const selectedProjects = this.getSelectedProject();
        if (selectedProjects.length === 0) {
            this.errorEvent.emit('You must select at least one project.');
            return;
        }

        this.sendSubmittedEvent();

        const dateRange = this.buildDateRange();

        const ballparkEffort$ = this.doAlgorithmExecution(selectedProjects, 
            ClusterGrouping.BALLPARK, ClusteringType.EFFORT_ONLY, dateRange);

        const ballparkCycle$ = this.doAlgorithmExecution(selectedProjects, 
            ClusterGrouping.BALLPARK, ClusteringType.CYCLE_ONLY, dateRange);

        const subtaskEffort$ = this.doAlgorithmExecution(selectedProjects, 
            ClusterGrouping.SUBTASK, ClusteringType.EFFORT_ONLY, dateRange);

        const subtaskCycle$ = this.doAlgorithmExecution(selectedProjects, 
            ClusterGrouping.SUBTASK, ClusteringType.CYCLE_ONLY, dateRange);

        zip(ballparkEffort$, ballparkCycle$, subtaskEffort$, subtaskCycle$)
            .pipe(
                map(results => {
                    const ballparkEffort = results[0].result;
                    const ballparkCycle = results[1].result;
                    const subtaskEffort = results[2].result;
                    const subtaskCycle = results[3].result;

                    const effort = _.extend({}, ballparkEffort, subtaskEffort);
                    const cycle = _.extend({}, ballparkCycle, subtaskCycle);

                    return this.getClusterItems(effort, cycle);
                })
            )
            .subscribe(
                executions => this.results.emit(executions),
                error => this.errorEvent.emit(error)
            );
    }

    getClusterItems(effort: any, cycle: any) {
        const clusterItems: ClusterItemDto[] = [];

        Object.keys(effort).forEach(issueType => {
            Object.keys(effort[issueType].clusters).forEach(tsize => {
                clusterItems.push(new ClusterItemDto(
                    this.convertClusterDaysToHours(cycle[issueType].clusters[tsize].centroid.values[0])
                    , effort[issueType].clusters[tsize].centroid.values[0]
                    , false
                    , issueType
                    , tsize
                ));
            });
        });

        return clusterItems;
    }

    convertClusterDaysToHours(value: number) {
        return value * 8;
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

        else if (this.tbClusterAlgorithm.control.get('endDate').invalid)
            this.endDateField.nativeElement.focus();
    }

    private sendSubmittedEvent() {
        this.submitted.emit(true);
    }

    private sendInvalidSubmitEvent() {
        this.submitted.emit(false);
    }
}
