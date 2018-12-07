import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TeamsComponent} from './teams/teams.component';
import {TeamComponent} from './teams/team/team.component';
import {AccessGuard, AuthRequirementsBuilder} from '../auth/access-guard';
import {LeaveConfirmationGuard} from '../shared/form-utils/leave-confirmation/guard/leave-confirmation.guard';
import {TEAM_EDIT, TEAMS_EDIT_VIEW} from '../auth/permissions';

const routes: Routes = [
    {
        path: 'teams',
        component: TeamsComponent,
        data: {
            requirements: AuthRequirementsBuilder.new()
                .permissions([TEAMS_EDIT_VIEW])
                .build()
        },
        canActivate: [ AccessGuard ]
    },
    {
        path: 'teams/:teamName',
        component: TeamComponent,
        data: {
            requirements: AuthRequirementsBuilder.new()
                .permissions([TEAM_EDIT])
                .paramPathPermissionPerKey('teamName')
                .build()
        },
        canActivate: [ AccessGuard ],
        canDeactivate: [ LeaveConfirmationGuard ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class TeamsRoutingModule {
}
