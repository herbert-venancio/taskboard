import { ClusterItemDto } from './cluster-item-dto.model';

export class ClusterItemDtoGroup {

    issueType: string;
    items: ClusterItemDto[] = [];
    changes: ClusterItemChangeDto[] = [];
    acceptChanges: {[key: string]: boolean} = {
        effort: false,
        cycle: false
    };

    get hasChanges(): boolean {
        return this.changes && this.changes.length > 0;
    }

    constructor(issueType: string, items?: ClusterItemDto[]) {
        this.issueType = issueType;
        if (items)
            this.items = items;
    }
}

function toFixed(value: number): number {
    return parseFloat(value.toFixed(2));
}

export class ClusterItemChangeDto extends ClusterItemDto {

    originalEffort: number;
    originalCycle: number;

    constructor(item: ClusterItemDto) {
        super(toFixed(item.cycle), toFixed(item.effort), item.fromBaseCluster, item.issueType, item.sizing);
        this.originalEffort = toFixed(item.effort);
        this.originalCycle = toFixed(item.cycle);
    }

}