import {Component, Inject, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {ProjectProfileItemDto, ProjectProfileService} from './project-profile.service';
import {LegacyAppRouter} from 'app/core/legacy-app-router';
import {PageSpinner} from 'app/core/page-spinner/page-spinner';
import {DATE_INPUT_DISPLAY_FORMAT} from 'app/shared/form-utils/date-input.directive';
import {NgForm} from '@angular/forms';
import * as moment from 'moment';
import {Moment} from 'moment';
import {SnackbarControl, SnackbarLevel} from 'app/shared/obj-ds/snackbar/snackbar-control';

@Component({
    selector: 'tb-project-profile',
    templateUrl: './project-profile.component.html',
    styleUrls: ['./project-profile.component.scss']
})
export class ProjectProfileComponent implements OnInit {
    private projectKey: string;
    private nextRowId = 0;

    items: ProjectProfileItemRow[] = [];
    newItem: ProjectProfileItemRow;
    snackbar = new SnackbarControl();

    constructor(
        private route: ActivatedRoute,
        private pageLoader: PageSpinner,
        private projectProfileService: ProjectProfileService,
        private legacyAppRouter: LegacyAppRouter,
        @Inject(DATE_INPUT_DISPLAY_FORMAT) public dateDisplayFormat: string
    ) {}

    ngOnInit() {
        this.route.parent.paramMap.subscribe((params: ParamMap) => {
            this.projectKey = params.get('key');
            this.refresh();
        });
    }

    private refresh() {
        this.pageLoader.show();
        this.nextRowId = 0;

        this.projectProfileService.getData(this.projectKey).subscribe(data => {
            this.items = data.map(dto => new ProjectProfileItemRow(this.nextRowId++, dto));
            this.pageLoader.hide();
        });
    }

    addItem() {
        const item = new ProjectProfileItemRow(this.nextRowId++);

        this.newItem = item;
        this.items.unshift(item);
    }

    removeItem(item: ProjectProfileItemRow) {
        const index = this.items.indexOf(item);
        this.items.splice(index, 1);
    }

    save(form: NgForm) {
        if (form.invalid) {
            this.snackbar.showInfo({title: 'Please review the form', level: SnackbarLevel.Error});
            return;
        }

        this.pageLoader.show();

        const dtos = this.items.map(row => {
            const dto = new ProjectProfileItemDto();
            dto.id = row.id;
            dto.roleName = row.roleName;
            dto.peopleCount = row.peopleCount;
            dto.allocationStart = row.allocationStart ? row.allocationStart.format('YYYY-MM-DD') : null;
            dto.allocationEnd = row.allocationEnd ? row.allocationEnd.format('YYYY-MM-DD') : null;

            return dto;
        });

        this.projectProfileService.updateItems(this.projectKey, dtos)
            .subscribe(
                () => {
                    this.pageLoader.hide();
                    this.snackbar.showInfo({title: 'Project profile saved', level: SnackbarLevel.Success});
                    form.form.markAsPristine();
                },
                error => {
                    this.pageLoader.hide();
                    this.snackbar.showInfo({title: 'Failed to save the project profile', level: SnackbarLevel.Error});
                });
    }

    backToProject() {
        this.legacyAppRouter.goToProjectConfiguration(this.projectKey);
    }
}

export class ProjectProfileItemRow {
    id: number;
    roleName: string;
    peopleCount: number;
    allocationStart: Moment;
    allocationEnd: Moment;

    constructor(readonly rowId: number, dto?: ProjectProfileItemDto) {
        if (dto) {
            this.id = dto.id;
            this.roleName = dto.roleName;
            this.peopleCount = dto.peopleCount;
            this.allocationStart = dto.allocationStart ? moment(dto.allocationStart) : null;
            this.allocationEnd = dto.allocationEnd ? moment(dto.allocationEnd) : null;
        }
    }
}
