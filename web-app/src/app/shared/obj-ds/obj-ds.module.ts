import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {AvatarComponent} from './avatar/avatar.component';
import {MenuComponent} from './menu/menu.component';
import {MenuToggleDirective} from './menu/menu-toggle.directive';
import {HttpClientModule} from '@angular/common/http';
import {SnackbarComponent} from './snackbar/snackbar.component';
import {ButtonModule} from './button/button.module';
import {CardModule} from './card/card.module';
import {IconModule} from './icon/icon.module';
import {FormInputsModule} from './form-inputs/form-inputs.module';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        HttpClientModule,
        ButtonModule,
        CardModule,
        IconModule,
        FormInputsModule
    ],
    declarations: [
        AvatarComponent,
        MenuComponent,
        MenuToggleDirective,
        SnackbarComponent
    ],
    exports: [
        AvatarComponent,
        ButtonModule,
        CardModule,
        IconModule,
        FormInputsModule,
        MenuComponent,
        MenuToggleDirective,
        SnackbarComponent
    ]
})
export class ObjDsModule {
}
