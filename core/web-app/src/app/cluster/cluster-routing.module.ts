import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AccessGuard, AuthRequirementsBuilder } from 'app/auth/access-guard';
import { BaseClusterSearchComponent } from './base-cluster-search/base-cluster-search.component';
import { BaseClusterComponent } from './base-cluster/base-cluster.component';
import { LeaveConfirmationGuard } from 'app/shared/form-utils/leave-confirmation/guard/leave-confirmation.guard';
import { TASKBOARD_ADMINISTRATION } from '../auth/permissions';

const routes: Routes = [
    {
        path: 'base-cluster-search',
        component: BaseClusterSearchComponent,
        data: {
            requirements: AuthRequirementsBuilder.new()
                .permissions([TASKBOARD_ADMINISTRATION])
                .build()
            },
        canActivate: [ AccessGuard ]
    },
    {
        path: 'base-cluster/new',
        component: BaseClusterComponent,
        data: {
            requirements: AuthRequirementsBuilder.new()
                .permissions([TASKBOARD_ADMINISTRATION])
                .build()
            },
        canActivate: [ AccessGuard ],
        canDeactivate: [ LeaveConfirmationGuard ]
    },
    {
        path: 'base-cluster/:id',
        component: BaseClusterComponent,
        data: {
            requirements: AuthRequirementsBuilder.new()
                .permissions([TASKBOARD_ADMINISTRATION])
                .build()
            },
        canActivate: [ AccessGuard ],
        canDeactivate: [ LeaveConfirmationGuard ]
    },
    {
        path: 'base-cluster',
        redirectTo: 'base-cluster/new'
    },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClusterRoutingModule { }
