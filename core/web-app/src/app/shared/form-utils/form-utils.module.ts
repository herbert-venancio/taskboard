import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {DateInputDirective} from './date-input.directive';
import {FocusDirective} from './focus.directive';
import {ValidatorsModule} from './validators/validators.module';
import {LeaveConfirmationDirective} from './leave-confirmation/leave-confirmation.directive';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ValidatorsModule
    ],
    declarations: [
        FocusDirective,
        DateInputDirective,
        LeaveConfirmationDirective
    ],
    exports: [
        ValidatorsModule,
        FocusDirective,
        DateInputDirective,
        LeaveConfirmationDirective
    ]
})
export class FormUtilsModule {}
