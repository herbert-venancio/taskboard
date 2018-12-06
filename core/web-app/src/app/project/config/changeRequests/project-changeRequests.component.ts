import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {ProjectChangeRequestDto, ProjectChangeRequestsService} from './project-changeRequests.service';
import {LegacyAppRouter} from 'app/core/legacy-app-router';
import {PageSpinner} from 'app/core/page-spinner/page-spinner';
import {DATE_INPUT_DISPLAY_FORMAT} from 'app/shared/form-utils/date-input.directive';
import {NgForm} from '@angular/forms';
import * as moment from 'moment';
import {Moment} from 'moment';
import {SnackbarControl, SnackbarLevel} from 'app/shared/obj-ds/snackbar/snackbar-control';
import {ComponentLeaveConfirmation} from '../../../shared/form-utils/leave-confirmation/guard/component-leave-confirmation';

@Component({
    selector: 'tb-project-changeRequests',
    templateUrl: './project-changeRequests.component.html',
    styleUrls: ['./project-changeRequests.component.scss']
})
export class ProjectChangeRequestsComponent extends ComponentLeaveConfirmation implements OnInit {

    @ViewChild('projectChangeRequestsForm') form: NgForm;

    private projectKey: string;
    private nextRowId = 0;

    items: ProjectChangeRequestRow[] = [];
    newItem: ProjectChangeRequestRow;
    snackbar = new SnackbarControl();
    total:number;

    constructor(
        private route: ActivatedRoute,
        private pageLoader: PageSpinner,
        private projectChangeRequestsService: ProjectChangeRequestsService,
        private legacyAppRouter: LegacyAppRouter,
        @Inject(DATE_INPUT_DISPLAY_FORMAT) public dateDisplayFormat: string
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
        this.nextRowId = 0;

        this.projectChangeRequestsService.getData(this.projectKey).subscribe(data => {
            this.items = data.map(dto => new ProjectChangeRequestRow(this.nextRowId++, dto));
            this.pageLoader.hide();
            this.updateTotal();
        });
    }
    
    addItem() {
        const item = new ProjectChangeRequestRow(this.nextRowId++);

        this.newItem = item;
        this.items.unshift(item);
        this.form.control.markAsDirty();
    }

    removeItem(item: ProjectChangeRequestRow) {
        const index = this.items.indexOf(item);
        this.items.splice(index, 1);
        this.updateTotal();
        this.form.control.markAsDirty();
    }

    updateTotal(){
        this.total = this.items.reduce((a, b) => +a + +b.budgetIncrease, 0);
    }

    save(form: NgForm) {
        if (form.invalid) {
            this.snackbar.showInfo({title: 'Please review the form', level: SnackbarLevel.Error});
            return;
        }

        this.pageLoader.show();

        const dtos = this.items.map(row => {
            const dto = new ProjectChangeRequestDto();
            dto.id = row.id;
            dto.name = row.name;
            dto.date = row.date ? row.date.format('YYYY-MM-DD') : null;
            dto.budgetIncrease = row.budgetIncrease;
            dto.isBaseline = row.isBaseline;

            return dto;
        });

        this.projectChangeRequestsService.updateItems(this.projectKey, dtos)
            .subscribe(
                () => {
                    this.pageLoader.hide();
                    this.snackbar.showInfo({title: 'Project change requests saved', level: SnackbarLevel.Success});
                    form.form.markAsPristine();
                },
                error => {
                    this.pageLoader.hide();
                    this.snackbar.showInfo({title: 'Failed to save the project change requests', level: SnackbarLevel.Error});
                });
    }
    
    backToProject() {
        this.legacyAppRouter.goToProjectConfiguration(this.projectKey);
    }

    canDeactivate(): boolean {
        return this.form.pristine;
    }
}

export class ProjectChangeRequestRow {
    id: number;
    name: string;
    date: Moment;
    budgetIncrease: number;
    isBaseline: boolean;

    constructor(readonly rowId: number, dto?: ProjectChangeRequestDto) {
        if (dto) {
            this.id = dto.id;
            this.name = dto.name;
            this.date = dto.date ? moment(dto.date) : null;
            this.budgetIncrease = dto.budgetIncrease;
            this.isBaseline = dto.isBaseline;
        }
    }
}
