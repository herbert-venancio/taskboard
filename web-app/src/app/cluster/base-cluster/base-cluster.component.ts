import {
    Component,
    OnInit,
    ViewChild
} from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';

import { PageSpinner } from 'app/core/page-spinner/page-spinner';
import { SnackbarControl, SnackbarLevel } from 'app/shared/obj-ds/snackbar/snackbar-control';
import { BaseClusterService } from 'app/cluster/base-cluster.service';
import { ComponentLeaveConfirmation } from 'app/shared/form-utils/leave-confirmation/guard/component-leave-confirmation';
import { BaseClusterDto } from './base-cluster-dto.model';
import { ClusterItemDto } from 'app/shared/tb-ds/forms/tb-cluster/cluster-item-dto.model';
import { TbClusterComponent } from 'app/shared/tb-ds/forms/tb-cluster/tb-cluster.component';

@Component({
  selector: 'tb-base-cluster',
  templateUrl: './base-cluster.component.html',
  styleUrls: ['./base-cluster.component.scss'],
  host: {
    class: 'tb-fixed-page'
  }
})
export class BaseClusterComponent extends ComponentLeaveConfirmation implements OnInit {

    baseClusterId: number;
    baseClusterName: string;

    snackbar = new SnackbarControl();
    @ViewChild(TbClusterComponent) clusterComponent: TbClusterComponent;

    clusterItems: ClusterItemDto[] = [];

    constructor(
        private title: Title,
        private spinner: PageSpinner,
        private route: ActivatedRoute,
        private router: Router,
        private service: BaseClusterService
    ) {
        super();
    }

    ngOnInit() {
        this.title.setTitle('Taskboard - Base Cluster');
        this.baseClusterId = this.route.snapshot.params['id'];
        this.loadCluster(this.baseClusterId);
    }

    canDeactivate(): boolean {
        return this.clusterComponent.isPristine();
    }

    save(itemsToUpdate: any) {
        try {
            this.spinner.show();

            const baseClusterDto = new BaseClusterDto();
            baseClusterDto.id = this.baseClusterId;
            baseClusterDto.name = this.baseClusterName;

            baseClusterDto.items = itemsToUpdate;

            if (this.baseClusterId)
                this.update(baseClusterDto);
            else
                this.create(baseClusterDto);

        } catch (error) {
            this.showMessage('Error', error, SnackbarLevel.Error);
        } finally {
            this.spinner.hide();
        }
    }

    callSaveOfClusterComponent() {
        this.clusterComponent.save();
    }

    showErrorFormMessage(message: string) {
        this.showMessage('Error', message, SnackbarLevel.Error);
    }

    private loadCluster(baseClusterId: number) {
        this.spinner.show();

        if (baseClusterId)
            this.findBaseCluster(baseClusterId);
        else
            this.setCreationModel();

        this.spinner.hide();
    }

    private update(dto: BaseClusterDto) {
        this.service.update(this.baseClusterId, dto)
            .subscribe(() => {
                this.showMessage('Success', 'Base cluster saved', SnackbarLevel.Success);
                this.markAsPristine();
            });
    }

    private create(dto: BaseClusterDto) {
        this.service.create(dto)
            .subscribe(response => {
                this.markAsPristine();

                const location = response.headers.get('location');
                const idGenerated = location.substr(location.lastIndexOf('/') + 1 );
                this.router.navigate(['/base-cluster', idGenerated], { fragment: 'new' });
            });
    }

    private markAsPristine(): any {
        this.clusterComponent.markAsPristine();
    }

    private findBaseCluster(baseClusterId: number) {
        this.service.findOne(baseClusterId)
            .subscribe(baseCluster => {
                this.setCurrentBaseCluster(baseCluster);
                this.showCreatedMessageIfNewBaseCluster();
                },
                error => this.showMessage('Error', 'Error to find the base cluster. Error: ' + JSON.stringify(error), SnackbarLevel.Error)
            );
    }

    private setCreationModel() {
        this.service.getNewModel()
            .subscribe(m => this.setCurrentBaseCluster(m));
    }

    private setCurrentBaseCluster(dto: BaseClusterDto) {
        this.baseClusterName = dto.name;
        this.clusterItems = dto.items;
    }

    private showCreatedMessageIfNewBaseCluster() {
        this.route.fragment.subscribe((fragment: string) => {
            if ('new' === fragment)
                this.showMessage('Success', 'Base cluster created', SnackbarLevel.Success);
        });
    }

    private showMessage(title: string, message: string, messageLevel: SnackbarLevel) {
        this.snackbar.showInfo({
            title: title,
            description: message,
            level: messageLevel
        });
    }
}
