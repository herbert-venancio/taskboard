import {Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {FormGroupDto} from '../../../shared/model-utils/form-group-dto.model';
import {SnackbarControl, SnackbarLevel} from '../../../shared/obj-ds/snackbar/snackbar-control';
import {PageSpinner} from '../../../core/page-spinner/page-spinner';
import {HttpErrorResponse} from '@angular/common/http';
import {SelectFieldAsyncSearchDto} from '../../../shared/model-utils/select-field-async-search-dto.model';
import {TeamDto} from '../team-dto.model';
import {TeamService} from './team.service';
import {ComponentLeaveConfirmation} from '../../../shared/form-utils/leave-confirmation/guard/component-leave-confirmation';


@Component({
    selector: 'tb-team',
    templateUrl: './team.component.html',
    styleUrls: ['./team.component.scss'],
    host: {
        class: 'tb-fixed-page'
    }
})
export class TeamComponent extends ComponentLeaveConfirmation implements OnInit {

    @ViewChild('teamForm') form: NgForm;

    private nextTeamGroupId: number;

    snackbar = new SnackbarControl();

    teamName: string;
    team: TeamDto;
    filterMembersByName: string = '';
    newMembers: NewTeamMemberRow[];

    managerAsyncSearch: SelectFieldAsyncSearchDto;

    constructor(
        private route: ActivatedRoute,
        private pageLoader: PageSpinner,
        private teamService: TeamService
    ) {
        super();
    }

    ngOnInit() {
        this.route.paramMap.subscribe((params: ParamMap) => {
            this.teamName = params.get('teamName');
            this.refreshPage();
        });
        this.managerAsyncSearch = new SelectFieldAsyncSearchDto((term: string) => this.teamService.getUsersWith(term));
    }

    private refreshPage(doneCallback?: Function) : void {
        this.pageLoader.show();
        this.nextTeamGroupId = 0;
        this.newMembers = [];

        this.teamService.getTeam(this.teamName).subscribe(team => {
            this.team = team;
            this.pageLoader.hide();
            if (doneCallback !== undefined)
                doneCallback();
        });
    }

    addMember() {
        const memberAsyncSearch = new SelectFieldAsyncSearchDto(
            (term: string) => this.teamService.getUsersWith(term),
            (value: string) => !this.isAlreadySelected(value)
        );
        const newMember = new NewTeamMemberRow(this.nextTeamGroupId++, null, memberAsyncSearch);
        this.newMembers.unshift(newMember);
        this.form.control.markAsDirty();
    }

    private isAlreadySelected(userName: string): boolean {
        const isMember = this.team.members.some(member => member === userName);
        const isNewMember = this.newMembers.some(member => member.dto === userName);
        return isMember || isNewMember;
    }

    removeNewMember(newMember: NewTeamMemberRow) {
        const index = this.newMembers.indexOf(newMember);
        this.newMembers.splice(index, 1);
        this.form.control.markAsDirty();
    }

    removeMember(userName: string) {
        const index = this.team.members.indexOf(userName);
        this.team.members.splice(index, 1);
        this.form.control.markAsDirty();
    }

    save() {
        if (this.form.invalid) {
            this.snackbar.showInfo({title: 'Please review the form', level: SnackbarLevel.Error});
            return;
        }

        this.pageLoader.show();

        const tempTeam = TeamDto.from(this.team, NewTeamMemberRow.getDtos(this.newMembers));

        this.teamService.updateTeam(tempTeam)
            .subscribe(
                () => {
                    this.refreshPage(() => {
                        this.snackbar.showInfo({title: 'Team saved', level: SnackbarLevel.Success});
                        this.form.control.markAsPristine();
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

    canDeactivate(): boolean {
        return this.form.pristine;
    }

}

export class NewTeamMemberRow extends FormGroupDto {

    memberAsyncSearch: SelectFieldAsyncSearchDto;

    constructor(groupId: number, dto: any, memberAsyncSearch: SelectFieldAsyncSearchDto) {
        super(groupId, dto);
        this.memberAsyncSearch = memberAsyncSearch;
    }

}
