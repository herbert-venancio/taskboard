import {Component, OnInit} from '@angular/core';
import {TeamsService} from './teams.service';
import {LegacyAppRouter} from '../../core/legacy-app-router';
import {TeamDto} from './team-dto.model';
import {PageSpinner} from '../../core/page-spinner/page-spinner';
import {MemberDto} from './member-dto.model';


@Component({
    selector: 'tb-teams',
    templateUrl: './teams.component.html',
    styleUrls: ['./teams.component.scss'],
    host: {
        class: 'tb-fixed-page'
    }
})
export class TeamsComponent implements OnInit {

    teams: TeamDto[] = [];

    filterTeamsByFields: string = '';

    constructor(
        private legacyAppRouter: LegacyAppRouter,
        private pageLoader: PageSpinner,
        private teamsService: TeamsService
    ) { }

    ngOnInit() {
        this.pageLoader.show();
        this.teamsService.getTeams().subscribe(teams => {
            this.teams = teams;
            this.pageLoader.hide();
        });
    }

    backToHome() {
        this.legacyAppRouter.goToHome();
    }

    getListMembersName(members: MemberDto[]) {
        return members.map(item => item.name).join(', ');
    }

}
