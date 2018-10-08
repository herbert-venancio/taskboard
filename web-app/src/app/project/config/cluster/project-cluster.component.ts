import { Component, OnInit, ViewChild, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { NgForm } from '@angular/forms';
import { LegacyAppRouter } from 'app/core/legacy-app-router';
import { PageSpinner } from 'app/core/page-spinner/page-spinner';
import { SnackbarControl, SnackbarLevel } from 'app/shared/obj-ds/snackbar/snackbar-control';
import { ExpansionPanelComponent } from 'app/shared/obj-ds/expansion-panel/expansion-panel.component';
import { ComponentLeaveConfirmation } from 'app/shared/form-utils/leave-confirmation/guard/component-leave-confirmation';
import { ProjectClusterService, ProjectClusterItemDto } from './project-cluster.service';

@Component({
    selector: 'tb-project-cluster',
    templateUrl: './project-cluster.component.html',
    styleUrls: ['./project-cluster.component.scss']
})
export class ProjectClusterComponent extends ComponentLeaveConfirmation implements OnInit {
    @ViewChild('projectClusterForm') form: NgForm;
    @ViewChildren(ExpansionPanelComponent) expansionPanels: QueryList<ExpansionPanelComponent>;
    @ViewChildren('sizingInput') inputs: QueryList<ElementRef>;

    private projectKey: string;

    hasBaseCluster = false;
    clusterIssueTypes: ProjectClusterIssueType[] = [];
    snackbar = new SnackbarControl();

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

        this.hasBaseCluster = false;
        this.projectClusterService.get(this.projectKey).subscribe(data => {
            data.forEach(dto => {
                if (!this.hasBaseCluster && dto.fromBaseCluster)
                    this.hasBaseCluster = true;

                const issueTypeFound = this.clusterIssueTypes.find(item => item.issueType === dto.issueType);
                if (!issueTypeFound) {
                    const newSizing = new ProjectClusterSizing(dto);
                    const newIssueType = new ProjectClusterIssueType(dto.issueType, [newSizing]);
                    this.clusterIssueTypes.push(newIssueType);
                    return;
                }

                const sizingFound = issueTypeFound.sizings.find(s => s.sizing === dto.sizing);
                if (!sizingFound) {
                    const newSizing = new ProjectClusterSizing(dto);
                    issueTypeFound.sizings.push(newSizing);
                    return;
                }

                sizingFound.effort = dto.effort;
                sizingFound.cycle = dto.cycle;
                sizingFound.fromBaseCluster = dto.fromBaseCluster;
            });
            this.expansionPanels.forEach(panel => panel.open());
            this.pageLoader.hide();
        });
    }

    backToProject() {
        this.legacyAppRouter.goToProjectConfiguration(this.projectKey);
    }

    save() {
        if (this.form.invalid) {
            this.snackbar.showInfo({title: 'Please review the form', level: SnackbarLevel.Error});
            this.expansionPanels.forEach(panel => {
                const panelControl = this.form.controls[panel.title];
                if (panelControl && panelControl.invalid)
                    panel.open();
            });
            const firstInvalidInput = this.inputs.find(i => !i.nativeElement.validity.valid);
            if (firstInvalidInput)
                firstInvalidInput.nativeElement.focus();
            return;
        }

        this.pageLoader.show();

        const itemsUpdated: ProjectClusterItemDto[] = [];
        this.clusterIssueTypes.forEach(item => {
            item.sizings.forEach(s => {
                const itemUpdated = new ProjectClusterItemDto();
                itemUpdated.projectKey = this.projectKey;
                itemUpdated.issueType = item.issueType;
                itemUpdated.sizing = s.sizing;
                itemUpdated.effort = s.effort;
                itemUpdated.cycle = s.cycle;
                itemUpdated.fromBaseCluster = s.fromBaseCluster;
                itemsUpdated.push(itemUpdated);
            });
        });

        this.projectClusterService.update(this.projectKey, itemsUpdated)
            .subscribe(
                () => {
                    this.pageLoader.hide();
                    this.snackbar.showInfo({title: 'Project cluster saved', level: SnackbarLevel.Success});
                    this.form.control.markAsPristine();
                    this.refresh();
                },
                error => {
                    this.pageLoader.hide();
                    this.snackbar.showInfo({title: 'Failed to save the project cluster', level: SnackbarLevel.Error});
                });
    }

    canDeactivate(): boolean {
        return this.form.pristine;
    }
}

export class ProjectClusterIssueType {
    issueType: string;
    sizings: ProjectClusterSizing[] = [];

    constructor(issueType: string, sizings: ProjectClusterSizing[]) {
        this.issueType = issueType;
        this.sizings = sizings;
    }
}

export class ProjectClusterSizing {
    sizing: string;
    effort: number;
    cycle: number;
    fromBaseCluster: boolean;

    constructor(dto: ProjectClusterItemDto) {
        this.sizing = dto.sizing;
        this.effort = dto.effort;
        this.cycle = dto.cycle;
        this.fromBaseCluster = dto.fromBaseCluster;
    }
}
