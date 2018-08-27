import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TextFieldDirective} from './text-field.directive';
import {FormsModule} from '@angular/forms';
import {ErrorMessagesComponent} from './error-messages/error-messages.component';
import {ErrorMessageComponent} from './error-messages/error-message.component';
import {SelectFieldDirective} from './select/select-field.directive';

@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    declarations: [
        TextFieldDirective,
        SelectFieldDirective,
        ErrorMessagesComponent,
        ErrorMessageComponent,
    ],
    exports: [
        TextFieldDirective,
        SelectFieldDirective,
        ErrorMessagesComponent,
        ErrorMessageComponent,
    ]
})
export class FormInputsModule {}
