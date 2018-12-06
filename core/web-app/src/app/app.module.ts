import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { APP_BASE_HREF } from '@angular/common';

import { AppRoutingModule } from './app.routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { ProjectModule } from './project/project.module';
import { TeamsModule } from './teams/teams.module';
import { ClusterModule } from './cluster/cluster.module';

@NgModule({
    imports: [
        BrowserModule,
        HttpClientModule,
        CoreModule,

        ProjectModule,
        TeamsModule,
        ClusterModule,

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
