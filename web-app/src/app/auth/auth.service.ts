import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoggedInUser} from './logged-in-user';
import {InitialDataService} from '../core/initial-data.service';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {LegacyAppRouter} from '../core/legacy-app-router';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private loggedInUser: Observable<LoggedInUser>;

    constructor(
        private httpClient: HttpClient,
        private initialDataService: InitialDataService,
        private legacyAppRouter: LegacyAppRouter) {

        this.loggedInUser = this.initialDataService.getData()
            .pipe(map(data => new LoggedInUser(data.loggedInUser.username, data.loggedInUser.name, data.loggedInUser.avatarUrl)));
    }

    getLoggedInUser(): Observable<LoggedInUser> {
        return this.loggedInUser;
    }

    logout(): void {
        localStorage.clear();
        sessionStorage.clear();

        this.httpClient.get('/ws/users/logout', {responseType: 'text'}).subscribe(() => {
            this.legacyAppRouter.goToHome();
        });
    }
}
