import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from './auth.service';
import {map} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class AccessGuard implements CanActivate {

    constructor(private authService: AuthService) {}

    canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
        if (route.data.requiresAdmin)
            return this.authService.getLoggedInUser().pipe(
                map(user => user.isAdmin)
            );

        return true;
    }

}
