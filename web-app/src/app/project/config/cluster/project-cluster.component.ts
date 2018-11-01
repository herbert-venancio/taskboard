import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';

import { LegacyAppRouter } from 'app/core/legacy-app-router';
import { PageSpinner } from 'app/core/page-spinner/page-spinner';
import { SnackbarControl, SnackbarLevel } from 'app/shared/obj-ds/snackbar/snackbar-control';
import { ComponentLeaveConfirmation } from 'app/shared/form-utils/leave-confirmation/guard/component-leave-confirmation';
import { ProjectClusterService } from './project-cluster.service';
import { ClusterItemDto } from 'app/shared/tb-ds/forms/tb-cluster/cluster-item-dto.model';
import { TbClusterComponent } from 'app/shared/tb-ds/forms/tb-cluster/tb-cluster.component';

@Component({
    selector: 'tb-project-cluster',
    templateUrl: './project-cluster.component.html',
    styleUrls: ['./project-cluster.component.scss']
})
export class ProjectClusterComponent extends ComponentLeaveConfirmation implements OnInit {

    private projectKey: string;

    hasBaseCluster = false;
    snackbar = new SnackbarControl();

    @ViewChild(TbClusterComponent) clusterComponent: TbClusterComponent;

    clusterItems: ClusterItemDto[] = [];

    constructor(
        private route: ActivatedRoute,
        private pageLoader: PageSpinner,
        private projectClusterService: ProjectClusterService,
        private legacyAppRouter: LegacyAppRouter
    ) {
        super();
    }

    ngOnInit() {
        this.route.parent.paramMap.subscribe((params: ParamMap) => {
            this.projectKey = params.get('key');
            this.refresh();
        });
    }

    private refresh() {
        this.pageLoader.show();
        this.clusterItems.splice(0);

    this.projectClusterService.get(this.projectKey)
        .subscribe(data => {
            this.clusterItems = data;
            this.hasBaseCluster = data.some(f => f.fromBaseCluster);
            this.pageLoader.hide();
        });
    }

    callSaveOfClusterComponent() {
        this.clusterComponent.save();
    }

    backToProject() {
        this.legacyAppRouter.goToProjectConfiguration(this.projectKey);
    }

    save(itemsToUpdate: any) {
        this.pageLoader.show();

        this.projectClusterService.update(this.projectKey, itemsToUpdate)
            .subscribe(
                () => {
                    this.pageLoader.hide();
                    this.showMessage('Success', 'Project cluster saved', SnackbarLevel.Success);
                    this.clusterComponent.markAsPristine();
                    this.refresh();
                },
                error => {
                    this.pageLoader.hide();
                    this.showMessage('Error', 'Failed to save the project cluster', SnackbarLevel.Error);
                });
    }

    showErrorFormMessage(message: string) {
        this.showMessage('Error', message, SnackbarLevel.Error);
    }

    canDeactivate(): boolean {
        return this.clusterComponent.isPristine();
    }

    private showMessage(title: string, message: string, level: SnackbarLevel) {
        this.snackbar.showInfo(
            {
                title: title,
                description: message,
                level: level
            });
    }
}
