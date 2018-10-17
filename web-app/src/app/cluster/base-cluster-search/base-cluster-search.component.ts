import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';

import { PageSpinner } from 'app/core/page-spinner/page-spinner';
import { SnackbarControl, SnackbarLevel } from 'app/shared/obj-ds/snackbar/snackbar-control';
import { LegacyAppRouter } from 'app/core/legacy-app-router';
import { BaseClusterService } from 'app/cluster/base-cluster.service';
import { NameableDto } from 'app/shared/model-utils/nameable-dto.model';

@Component({
  selector: 'tb-base-cluster-search',
  templateUrl: './base-cluster-search.component.html',
  styleUrls: ['./base-cluster-search.component.scss'],
  host: {
    class: 'tb-fixed-page'
  }
})
export class BaseClusterSearchComponent implements OnInit {

    clusterId: number;
    baseClusters: NameableDto[] = [];
    filterClustersByFields: string;
    snackbar = new SnackbarControl();

    constructor(
        private title: Title,
        private spinner: PageSpinner,
        private legacyRouter: LegacyAppRouter,
        private service: BaseClusterService
        ) { }

    ngOnInit() {
        this.title.setTitle('Taskboard - Base Cluster Search');
        this.loadBaseClusters();
    }

    backToHome() {
        this.legacyRouter.goToHome();
    }

    private loadBaseClusters() {
        this.spinner.show();

        this.service.findAll()
            .subscribe(r =>
                r.forEach(c => this.baseClusters.push({id: c.id, name: c.name})),
                error => this.showMessage('Error', 'Error to find the base clusters. Error:' + error, SnackbarLevel.Error)
            );
        this.spinner.hide();
    }

    private showMessage(title: string, message: string, messageLevel: SnackbarLevel) {
        this.snackbar.showInfo({
            title: title,
            description: message,
            level: messageLevel
        });
    }
}
