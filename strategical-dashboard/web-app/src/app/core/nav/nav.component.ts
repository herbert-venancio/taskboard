import { Component, OnInit } from '@angular/core';
import { LegacyAppRouter } from '../../core/legacy-app-router';
import { AuthService } from 'app/auth/auth.service';
import { LoggedInUser } from '../../auth/logged-in-user';
import {HttpClient} from '@angular/common/http';

@Component({
    selector: 'sdb-nav',
    templateUrl: './nav.component.html',
    styleUrls: ['./nav.component.scss']
})
export class NavComponent implements OnInit {
    title: String = 'Taskboard';
    loggedInUser: LoggedInUser;

    constructor(
        private httpClient: HttpClient,
        private legacyAppRouter: LegacyAppRouter,
        private authService: AuthService
    ) {}

    ngOnInit() {
        this.authService.getLoggedInUser().subscribe(user => this.loggedInUser = user);
    }

    logout() {
        this.authService.logout();
    }

    cleanCache() {
        this.httpClient.get('/cache/dashboard', {responseType: 'text'}).subscribe(() => {
            location.reload();
        });
    }

    backToHome() {
        this.legacyAppRouter.goToHome();
    }
}
