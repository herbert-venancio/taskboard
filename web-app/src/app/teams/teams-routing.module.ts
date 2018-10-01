import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TeamsComponent} from './teams/teams.component';
import {TeamComponent} from './teams/team/team.component';
import {AccessGuard} from '../auth/access-guard';
import {LeaveConfirmationGuard} from '../shared/form-utils/leave-confirmation/guard/leave-confirmation.guard';

const routes: Routes = [
    {
        path: 'teams',
        component: TeamsComponent,
        data: { requiresAdmin: true },
        canActivate: [ AccessGuard ]
    },
    {
        path: 'teams/:teamName',
        component: TeamComponent,
        data: { requiresAdmin: true },
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
