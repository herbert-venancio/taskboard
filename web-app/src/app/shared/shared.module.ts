import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

import {ObjDsModule} from './obj-ds/obj-ds.module';
import {FormUtilsModule} from './form-utils/form-utils.module';

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
        ObjDsModule
    ]
})
export class SharedModule {}
