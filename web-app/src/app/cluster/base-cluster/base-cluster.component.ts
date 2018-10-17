import { Title } from '@angular/platform-browser';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit, ViewChild, ViewChildren, QueryList, ElementRef } from '@angular/core';

import { PageSpinner } from 'app/core/page-spinner/page-spinner';
import { SnackbarControl, SnackbarLevel } from 'app/shared/obj-ds/snackbar/snackbar-control';
import { BaseClusterService, BaseClusterItemDto, BaseClusterDto } from 'app/cluster/base-cluster.service';
import { ExpansionPanelComponent } from 'app/shared/obj-ds/expansion-panel/expansion-panel.component';
import { ComponentLeaveConfirmation } from 'app/shared/form-utils/leave-confirmation/guard/component-leave-confirmation';

@Component({
  selector: 'tb-base-cluster',
  templateUrl: './base-cluster.component.html',
  styleUrls: ['./base-cluster.component.scss'],
  host: {
    class: 'tb-fixed-page'
  }
})
export class BaseClusterComponent extends ComponentLeaveConfirmation implements OnInit {

    @ViewChild('baseClusterForm') form: NgForm;
    @ViewChildren('clusterInput') inputs: QueryList<ElementRef>;
    @ViewChildren(ExpansionPanelComponent) expansionPanels: QueryList<ExpansionPanelComponent>;

    baseClusterId: number;
    baseClusterName: string;
    baseItemsGroups: BaseClusterItemDtoGroup[] = [];
    snackbar = new SnackbarControl();

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
        return this.form.pristine;
    }

    save() {
        try {
            this.spinner.show();

            if (this.form.invalid) {
                this.showMessage('Error', 'Please review the form', SnackbarLevel.Error);
                this.expandAllPanels();
                this.focusOnFirstInputWithError();
                return;
            }
            const baseClusterDto = new BaseClusterDto();
            baseClusterDto.id = this.baseClusterId;
            baseClusterDto.name = this.baseClusterName;

            this.baseItemsGroups.forEach(group =>
                group.items.forEach(i => baseClusterDto.items.push(i))
            );

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
                this.form.control.markAsPristine();
            });
    }

    private create(dto: BaseClusterDto) {
        this.service.create(dto)
            .subscribe(response => {
                this.form.control.markAsPristine();

                const location = response.headers.get('location');
                const idGenerated = location.substr(location.lastIndexOf('/') + 1 );
                this.router.navigate(['/base-cluster', idGenerated], { fragment: 'new' });
            });
    }

    private findBaseCluster(baseClusterId: number) {
        this.service.findOne(baseClusterId)
            .subscribe(baseCluster => {
                    this.setCurrentBaseCluster(baseCluster);
                    this.showCreatedMessageIfNewBaseCluster();
                },
                error => this.showMessage('Error', 'Error to find the base cluster. Error: ' + error, SnackbarLevel.Error)
            );
    }

    private setCreationModel() {
        this.service.getNewModel()
            .subscribe(m => this.setCurrentBaseCluster(m));
    }

    private expandAllPanels(): void {
        this.expansionPanels
            .filter(p => {
                const panelControl = this.form.controls[p.title];
                return panelControl && panelControl.invalid;
            })
            .forEach(p => p.open());
    }

    private focusOnFirstInputWithError(): void {
        this.inputs.find(i => !i.nativeElement.validity.valid)
            .nativeElement.focus();
    }

    private setCurrentBaseCluster(dto: BaseClusterDto) {
        this.baseClusterName = dto.name;

        dto.items.forEach(i => {
            let groupFound = this.baseItemsGroups.find(item =>  item.subtaskTypeName === i.subtaskTypeName);

            if (!groupFound) {
                groupFound = new BaseClusterItemDtoGroup(i.subtaskTypeName);
                this.baseItemsGroups.push(groupFound);
            }
            groupFound.items.push(i);
        });
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

class BaseClusterItemDtoGroup {
    subtaskTypeName: string;
    items: BaseClusterItemDto[] = [];

    constructor(subtaskTypeName: string) {
        this.subtaskTypeName = subtaskTypeName;
    }
}
