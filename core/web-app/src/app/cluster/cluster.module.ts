import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';

import { ClusterRoutingModule } from './cluster-routing.module';
import { SharedModule } from 'app/shared/shared.module';
import { BaseClusterComponent } from './base-cluster/base-cluster.component';
import { BaseClusterSearchComponent } from './base-cluster-search/base-cluster-search.component';

@NgModule({
  imports: [
    CommonModule,
    NgSelectModule,
    FormsModule,
    SharedModule,
    ClusterRoutingModule
  ],
  declarations: [
      BaseClusterComponent,
      BaseClusterSearchComponent
    ]
})
export class ClusterModule { }
