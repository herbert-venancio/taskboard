export class ClusterAlgorithmRequest {
    projects: string[];
    featureIssueTypes: number[];
    bugIssueTypes: number[];
    featureDoneStatuses: number[];
    subtaskDoneStatuses: number[];
    subtaskCycleStatuses: CycleStatuses;
    clusterGrouping: ClusterGrouping;
    clusteringType: ClusteringType;
    dateRange: DateRange;
}

export class CycleStatuses {
    first: number;
    last: number;

    constructor(first: number, last: number) {
        this.first = first;
        this.last = last;
    }
}

export enum ClusterGrouping {
    BALLPARK = 'BALLPARK',
    SUBTASK = 'SUBTASK'
}

export enum ClusteringType {
    EFFORT_ONLY = 'EFFORT_ONLY',
    CYCLE_ONLY = 'CYCLE_ONLY',
    EFFORT_AND_CYCLE = 'EFFORT_AND_CYCLE'
}

export class DateRange {
    startDate: string;
    endDate: string;

    constructor(startDate: string, endDate: string) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

export class ClusterAlgorithmExecution {
    executionId: number;
    request: ClusterAlgorithmRequest;
    executionStart: string;
    executionStop: string;
    status: Status;
    progress: number;
    result: {[key: string]: ClusterAlgorithmResult };
}

export enum Status {
    created = 'created',
    running = 'running',
    finished = 'finished',
    error = 'error',
    cancelled = 'cancelled'
}

export class ClusterAlgorithmResult {
    clusters: {[key: string]: Cluster};
    outliers: Array<IssueModel>;
}

export class Cluster {
    centroid: Centroid;
    points: Array<IssueModel>;
}

export class Centroid {
    axis: Array<string>;
    values: Array<number>;
}

export class IssueModel {
    issueKey: string;
    workedTime: number;
    cycleDays: number;
}
