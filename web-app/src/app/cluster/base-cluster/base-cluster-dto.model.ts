import { ClusterItemDto } from 'app/shared/tb-ds/forms/tb-cluster/cluster-item-dto.model';

export class BaseClusterDto {
    id: number;
    name: string;
    items: ClusterItemDto[] = [];
}
