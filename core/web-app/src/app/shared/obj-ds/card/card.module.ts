import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CardActionsComponent} from './card-actions.component';
import {CardTitleComponent} from './card-title.component';
import {CardContentComponent} from './card-content.component';
import {CardComponent} from './card.component';
import {IconModule} from '../icon/icon.module';

@NgModule({
    imports: [
        CommonModule,
        IconModule
    ],
    declarations: [
        CardComponent,
        CardTitleComponent,
        CardActionsComponent,
        CardContentComponent
    ],
    exports: [
        CardComponent,
        CardTitleComponent,
        CardActionsComponent,
        CardContentComponent
    ]
})
export class CardModule {}
