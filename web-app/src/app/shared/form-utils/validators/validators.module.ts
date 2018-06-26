import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {NotEmptyDirective} from './not-empty.directive';
import {MinDirective} from './min.directive';
import {IsNumberDirective} from './is-number.directive';
import {SameOrBeforeDirective} from './same-or-before.directive';
import {SameOrAfterDirective} from './same-or-after.directive';

@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    declarations: [
        NotEmptyDirective,
        IsNumberDirective,
        MinDirective,
        SameOrBeforeDirective,
        SameOrAfterDirective
    ],
    exports: [
        NotEmptyDirective,
        IsNumberDirective,
        MinDirective,
        SameOrBeforeDirective,
        SameOrAfterDirective
    ]
})
export class ValidatorsModule {}
