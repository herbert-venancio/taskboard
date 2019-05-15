import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { APP_BASE_HREF } from '@angular/common';

import { AppRoutingModule } from './app.routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { ObjectiveDesignSystemModule } from 'objective-design-system';
import { DashboardModule } from './dashboard/dashboard.module';

@NgModule({
    imports: [
        BrowserModule,
        HttpClientModule,
        CoreModule,

        ObjectiveDesignSystemModule,
        DashboardModule,
        AppRoutingModule
    ],
    declarations: [AppComponent],
    providers: [
        { provide: APP_BASE_HREF, useValue: '/strategical-dashboard/' }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
