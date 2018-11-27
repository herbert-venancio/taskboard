import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {PageSpinner} from '../../../core/page-spinner/page-spinner';
import {LegacyAppRouter} from '../../../core/legacy-app-router';
import {ProjectTeamByIssueTypeDto, ProjectTeamsService} from './project-teams.service';
import {SnackbarControl, SnackbarLevel} from '../../../shared/obj-ds/snackbar/snackbar-control';
import {NgForm} from '@angular/forms';
import {HttpErrorResponse} from "@angular/common/http";
import {NameableDto} from "../../../shared/model-utils/nameable-dto.model";
import {FormGroupDto} from "../../../shared/model-utils/form-group-dto.model";

@Component({
    selector: 'tb-project-teams',
    templateUrl: './project-teams.component.html',
    styleUrls: ['./project-teams.component.scss']
})
export class ProjectTeamsComponent implements OnInit {

    private projectKey: string;
    private nextDefaultTeamByIssueTypeGroupId = 0;

    public snackbar = new SnackbarControl();

    public defaultTeamId: number;
    public defaultTeamDisabled: boolean;
    public defaultTeamsByIssueType: FormGroupDto[] = [];
    public teams: NameableDto[] = [];
    public issueTypes: NameableDto[] = [];

    constructor(
        private route: ActivatedRoute,
        private pageLoader: PageSpinner,
        private legacyAppRouter: LegacyAppRouter,
        private projectTeamsService: ProjectTeamsService,
    ) {}

    ngOnInit() {
        this.route.parent.paramMap.subscribe((params: ParamMap) => {
            this.projectKey = params.get('key');
            this.refreshPage();
        });
    }

    backToProject() {
        this.legacyAppRouter.goToProjectConfiguration(this.projectKey);
    }

    addDefaultTeamByIssueType() {
        const defaultTeamByIssueType = new FormGroupDto(this.nextDefaultTeamByIssueTypeGroupId++, new ProjectTeamByIssueTypeDto());
        this.defaultTeamsByIssueType.unshift(defaultTeamByIssueType);
    }

    removeDefaultTeamByIssueType(defaultTeamByIssueTypeGroup: FormGroupDto) {
        const index = this.defaultTeamsByIssueType.indexOf(defaultTeamByIssueTypeGroup);
        this.defaultTeamsByIssueType.splice(index, 1);
    }

    save(form: NgForm) {
        if (form.invalid) {
            this.snackbar.showInfo({title: 'Please review the form', level: SnackbarLevel.Error});
            return;
        }

        this.pageLoader.show();

        this.projectTeamsService.updateTeams(this.projectKey, this.defaultTeamId, FormGroupDto.getDtos(this.defaultTeamsByIssueType))
            .subscribe(
                () => {
                    this.refreshPage(() => {
                        this.snackbar.showInfo({title: 'Project teams saved', level: SnackbarLevel.Success});
                        form.form.markAsPristine();
                    });
                },
                (errorResponse: HttpErrorResponse) => {
                    this.pageLoader.hide();
                    this.snackbar.showInfo({
                        title: 'Failed to save',
                        level: SnackbarLevel.Error,
                        description: errorResponse.error
                    });
                });
    }

    private refreshPage(doneCallback?: Function) {
        this.pageLoader.show();
        this.nextDefaultTeamByIssueTypeGroupId = 0;

        this.projectTeamsService.getData(this.projectKey).subscribe(data => {
            this.teams = data.teams;
            this.issueTypes = data.issueTypes;
            this.defaultTeamId = data.defaultTeamId;
            this.defaultTeamsByIssueType = data.defaultTeamsByIssueType.map(i => new FormGroupDto(this.nextDefaultTeamByIssueTypeGroupId++, i));
            this.pageLoader.hide();
            if (doneCallback !== undefined)
                doneCallback();
        });
    }

}
