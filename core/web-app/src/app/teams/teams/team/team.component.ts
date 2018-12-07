import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
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
import {MemberDto} from '../member-dto.model';
import {Role} from 'app/shared/enum-utils/role-enum';

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
    @ViewChild('listMembers') listMembers: ElementRef;

    private nextTeamGroupId: number;

    snackbar = new SnackbarControl();

    teamName: string;
    team: TeamDto;
    filterMembersByName: string = '';
    newMembers: NewTeamMemberRow[];
    roles: string[] = Object.keys(Role);
    focusOn: string;

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

    private refreshPage(doneCallback?: Function): void {
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
        const newMember = new NewTeamMemberRow(this.nextTeamGroupId++, new MemberDto(), memberAsyncSearch);
        this.newMembers.unshift(newMember);
        this.form.control.markAsDirty();
        this.listMembers.nativeElement.scrollTop = 0;
    }

    private isAlreadySelected(userName: string): boolean {
        const isMember = this.team.members.some(member => member.name === userName);
        const isNewMember = this.newMembers.some(member => member.dto === userName);
        return isMember || isNewMember;
    }

    removeNewMember(newMember: NewTeamMemberRow) {
        const index = this.newMembers.indexOf(newMember);
        this.newMembers.splice(index, 1);
        this.form.control.markAsDirty();
    }

    removeMember(userName: string) {
        this.team.members = this.team.members.filter(member => member.name !== userName);
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

    setFocusElement(index: string) {
        this.focusOn = 'new-member-role-' + index;
    }

    clearFocusOn() {
        this.focusOn = '';
    }

    trackByName(index: number, member: MemberDto) {
        return member.name;
    }

}

export class NewTeamMemberRow extends FormGroupDto {

    memberAsyncSearch: SelectFieldAsyncSearchDto;

    constructor(groupId: number, dto: MemberDto, memberAsyncSearch: SelectFieldAsyncSearchDto) {
        super(groupId, dto);
        this.memberAsyncSearch = memberAsyncSearch;
    }

}
