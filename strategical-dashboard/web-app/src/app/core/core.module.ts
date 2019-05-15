import { NgModule } from '@angular/core';
import { NavComponent } from './nav/nav.component';
import { PageSpinnerComponent } from './page-spinner/page-spinner.component';
import { PageNotFoundComponent } from './common-pages/page-not-found.component';
import { ObjectiveDesignSystemModule } from 'objective-design-system';

@NgModule({
    imports: [
        ObjectiveDesignSystemModule
    ],
    declarations: [
        NavComponent,
        PageSpinnerComponent,
        PageNotFoundComponent
    ],
    exports: [
        NavComponent,
        PageSpinnerComponent,
        PageNotFoundComponent
    ]
})
export class CoreModule {
}
