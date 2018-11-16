import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
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
import {TabsRouterComponent} from './tabs/tabs-router/tabs-router.component';
import {TabRouterComponent} from './tabs/tabs-router/tab-router/tab-router.component';
import {RouterModule} from '@angular/router';
import {DataTableTopComponent} from './data-table/top/data-table-top.component';
import {TitleComponent} from './title/title.component';
import {HeaderContentComponent} from './header-content/header-content.component';
import {LabelFieldComponent} from './label-field/label-field.component';
import {TagComponent} from './tag/tag.component';
import {ExpansionPanelComponent} from './expansion-panel/expansion-panel.component';
import {ModalComponent} from './modal/modal.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        HttpClientModule,
        ButtonModule,
        CardModule,
        IconModule,
        FormInputsModule,
        RouterModule
    ],
    declarations: [
        AvatarComponent,
        MenuComponent,
        MenuToggleDirective,
        SnackbarComponent,
        TabsRouterComponent,
        TabRouterComponent,
        DataTableTopComponent,
        TitleComponent,
        HeaderContentComponent,
        LabelFieldComponent,
        TagComponent,
        ExpansionPanelComponent,
        ModalComponent
    ],
    exports: [
        AvatarComponent,
        ButtonModule,
        CardModule,
        IconModule,
        FormInputsModule,
        MenuComponent,
        MenuToggleDirective,
        SnackbarComponent,
        TabsRouterComponent,
        TabRouterComponent,
        DataTableTopComponent,
        TitleComponent,
        HeaderContentComponent,
        LabelFieldComponent,
        TagComponent,
        ExpansionPanelComponent,
        ModalComponent
    ],
    schemas: [
        NO_ERRORS_SCHEMA
    ]
})
export class ObjDsModule {
}
