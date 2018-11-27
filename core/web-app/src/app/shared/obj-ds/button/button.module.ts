import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FlatButtonComponent} from './flat-button.component';
import {ButtonComponent} from './button.component';
import {IconButtonComponent} from './icon-button.component';
import {IconModule} from '../icon/icon.module';

@NgModule({
    imports: [
        CommonModule,
        IconModule
    ],
    declarations: [
        ButtonComponent,
        IconButtonComponent,
        FlatButtonComponent
    ],
    exports: [
        IconModule,
        ButtonComponent,
        IconButtonComponent,
        FlatButtonComponent
    ]
})
export class ButtonModule {}
