import {Component, OnInit} from '@angular/core';
import {AuthService} from 'app/auth/auth.service';
import {LoggedInUser} from '../../auth/logged-in-user';

@Component({
    selector: 'tb-nav',
    templateUrl: './nav.component.html',
    styleUrls: ['./nav.component.scss']
})
export class NavComponent implements OnInit {
    title: String = 'Taskboard';
    loggedInUser: LoggedInUser;

    constructor(private authService: AuthService) {}

    ngOnInit() {
        this.authService.getLoggedInUser().subscribe(user => this.loggedInUser = user);
    }

    logout() {
        this.authService.logout();
    }
}
