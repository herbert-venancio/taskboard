import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {APP_BASE_HREF} from '@angular/common';

import {AppRoutingModule} from './app.routing.module';
import {AppComponent} from './app.component';
import {ProjectModule} from './project/project.module';
import {CoreModule} from './core/core.module';

@NgModule({
    imports: [
        BrowserModule,
        HttpClientModule,
        CoreModule,

        ProjectModule,

        AppRoutingModule // Order matters and this should be the last import.
    ],
    declarations: [AppComponent],
    providers: [
        {provide: APP_BASE_HREF, useValue: '/app/'}
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
