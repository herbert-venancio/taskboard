import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { NgSelectModule } from '@ng-select/ng-select';
import { CommonModule } from '@angular/common';

import { ObjDsModule } from '../obj-ds/obj-ds.module';
import { FormUtilsModule } from './../form-utils/form-utils.module';
import { TbClusterComponent } from './forms/tb-cluster/tb-cluster.component';
import { TbClusterAlgorithmComponent } from './forms/tb-cluster-algorithm/tb-cluster-algorithm.component';

@NgModule({
    imports: [
        CommonModule,
        NgSelectModule,
        BrowserModule,
        FormsModule,
        ObjDsModule,
        FormUtilsModule
    ],
    declarations: [
        TbClusterComponent,
        TbClusterAlgorithmComponent
    ],
    exports: [
        TbClusterComponent,
        TbClusterAlgorithmComponent
    ]
})
export class TbDsModule { }
