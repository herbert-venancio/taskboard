import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TextFieldDirective} from './text-field.directive';
import {FormsModule} from '@angular/forms';
import {ErrorMessagesComponent} from './error-messages/error-messages.component';
import {ErrorMessageComponent} from './error-messages/error-message.component';
import {SelectFieldDirective} from './select/select-field.directive';
import {SearchFieldComponent} from "./search/search-field.component";
import {IconModule} from "../icon/icon.module";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        IconModule
    ],
    declarations: [
        TextFieldDirective,
        SelectFieldDirective,
        SearchFieldComponent,
        ErrorMessagesComponent,
        ErrorMessageComponent,
    ],
    exports: [
        TextFieldDirective,
        SelectFieldDirective,
        SearchFieldComponent,
        ErrorMessagesComponent,
        ErrorMessageComponent,
    ]
})
export class FormInputsModule {}
