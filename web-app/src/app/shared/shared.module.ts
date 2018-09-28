import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

import {ObjDsModule} from './obj-ds/obj-ds.module';
import {FormUtilsModule} from './form-utils/form-utils.module';
import {ObjFilterPipe} from "./pipes/obj-filter/obj-filter.pipe";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        FormUtilsModule,
        ObjDsModule
    ],
    exports: [
        CommonModule,
        FormsModule,
        FormUtilsModule,
        ObjDsModule,
        ObjFilterPipe
    ],
    declarations: [
        ObjFilterPipe
    ],
})
export class SharedModule {}
