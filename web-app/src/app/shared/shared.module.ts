import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { ObjDsModule } from './obj-ds/obj-ds.module';
import { FormUtilsModule } from './form-utils/form-utils.module';
import { ObjFilterPipe } from './pipes/obj-filter/obj-filter.pipe';
import { SortByPipe } from './pipes/sortBy/sortby.pipe';
import { TbDsModule } from './tb-ds/tb-ds.module';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        FormUtilsModule,
        ObjDsModule,
        TbDsModule
    ],
    exports: [
        CommonModule,
        FormsModule,
        FormUtilsModule,
        ObjDsModule,
        ObjFilterPipe,
        SortByPipe,
        TbDsModule
    ],
    declarations: [
        ObjFilterPipe,
        SortByPipe
    ],
})
export class SharedModule {}
