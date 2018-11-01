import { ClusterItemDto } from './cluster-item-dto.model';

export class ClusterItemDtoGroup {
    issueType: string;
    items: ClusterItemDto[] = [];

    constructor(issueType: string) {
        this.issueType = issueType;
    }
}
