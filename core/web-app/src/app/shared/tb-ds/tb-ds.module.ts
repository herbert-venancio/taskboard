import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { NgSelectModule } from '@ng-select/ng-select';
import { CommonModule } from '@angular/common';

import { ObjDsModule } from '../obj-ds/obj-ds.module';
import { FormUtilsModule } from './../form-utils/form-utils.module';
import { TbClusterComponent } from './forms/tb-cluster/tb-cluster.component';
import { TbModalComponent } from './modal/tb-modal.component';
import { TbModalContentComponent } from './modal/tb-modal-content.component';
import { TbModalFooterLeftComponent, TbModalFooterRightComponent } from './modal/tb-modal-footer.component';
import { TbToolbarComponent } from './toolbar/tb-toolbar.component';

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
        TbModalComponent,
        TbModalContentComponent,
        TbModalFooterLeftComponent,
        TbModalFooterRightComponent,
        TbToolbarComponent
    ],
    exports: [
        TbClusterComponent,
        TbModalComponent,
        TbModalContentComponent,
        TbModalFooterLeftComponent,
        TbModalFooterRightComponent,
        TbToolbarComponent
    ]
})
export class TbDsModule { }
