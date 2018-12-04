import { Component, EventEmitter, Output, ViewChild, Input } from '@angular/core';
import { PageSpinner } from 'app/core/page-spinner/page-spinner';
import { TbClusterAlgorithmComponent } from 'app/shared/tb-ds/forms/tb-cluster-algorithm/tb-cluster-algorithm.component';
import { ClusterItemDto } from 'app/shared/tb-ds/forms/tb-cluster/cluster-item-dto.model';
import { ModalComponent } from 'app/shared/obj-ds/modal/obj-modal.component';
import { Moment } from 'moment';

export class RecalculateResult {
    constructor(
        public newClusters: ClusterItemDto[],
        public startDate: Moment,
        public endDate: Moment
    ) { }
}

@Component({
    selector: 'tb-project-cluster-recalculate-modal',
    templateUrl: './project-cluster-recalculate-modal.component.html'
})
export class ProjectClusterRecalculateModalComponent {

    @Input('project')
    project: string;

    @Output('result')
    result = new EventEmitter<RecalculateResult>();
    @Output('errorEvent')
    errorEvent = new EventEmitter();

    @ViewChild(TbClusterAlgorithmComponent) algorithmComponent: TbClusterAlgorithmComponent;
    @ViewChild(ModalComponent) algorithmComponentModal: ModalComponent;

    constructor(
        private pageLoader: PageSpinner
    ) {

    }

    open() {
        this.algorithmComponentModal.open();
    }

    onResults(newClusters: ClusterItemDto[]) {
        this.pageLoader.hide();
        this.result.emit(new RecalculateResult(newClusters, this.algorithmComponent.startDate, this.algorithmComponent.endDate));
    }

    onErrors(error: any) {
        this.pageLoader.hide();
        this.errorEvent.emit(JSON.stringify(error));
    }

    closeModal(submitted: boolean) {
        if (submitted) {
            this.algorithmComponentModal.close();
            this.pageLoader.show();
        }
    }
}
