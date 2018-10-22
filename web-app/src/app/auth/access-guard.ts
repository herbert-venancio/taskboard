import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from './auth.service';
import {map} from 'rxjs/operators';
import {LoggedInUser} from "./logged-in-user";

@Injectable({
    providedIn: 'root'
})
export class AccessGuard implements CanActivate {

    constructor(private authService: AuthService) {}

    canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
        if (this.hasRequirements(route.data.requirements))
            return this.authService.getLoggedInUser().pipe(
                map(user => this.isAllowed(user, route.data.requirements))
            );

        return true;
    }

    private hasRequirements(requirements: Requirements): boolean {
        return requirements !== undefined && requirements !== null && requirements.permissions.length > 0;
    }

    private isAllowed(user: LoggedInUser, requirements: Requirements): boolean {
        return requirements.permissions.every(tp => {
            return user.hasPermission(tp);
        });
    }

}

export class Requirements {
    permissions: string[] = [];
}

export class RequirementsBuilder {

    private requirements = new Requirements();

    public static new(): RequirementsBuilder {
        return new RequirementsBuilder();
    }

    public permissions(permissions: string[]): RequirementsBuilder {
        this.requirements.permissions = permissions;
        return this;
    }

    public build(): Requirements {
        return this.requirements;
    }
}
