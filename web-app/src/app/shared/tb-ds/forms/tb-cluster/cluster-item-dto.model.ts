export class ClusterItemDto {
    cycle: number;
    effort: number;
    fromBaseCluster: boolean;
    issueType: string;
    projectKey: string;
    sizing: string;

    constructor(
        cycle: number,
        effort: number,
        fromBaseCluster: boolean,
        issueType: string,
        projectKey: string,
        sizing: string
    ) {
        this.cycle = cycle;
        this.effort = effort;
        this.fromBaseCluster = fromBaseCluster;
        this.issueType = issueType;
        this.projectKey = projectKey;
        this.sizing = sizing;
    }
}
