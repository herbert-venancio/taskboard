import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from './auth.service';
import {map} from 'rxjs/operators';
import {LoggedInUser} from './logged-in-user';

@Injectable({
    providedIn: 'root'
})
export class AccessGuard implements CanActivate {

    constructor(private authService: AuthService) {}

    canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
        if (route.data.requirements.hasOwnProperty('permissionPerKey'))
            return this.authService.getLoggedInUser().pipe(
                map(user => this.isAllowedPerKey(user, route.data.requirements,
                    route.params[route.data.requirements.permissionPerKey]))
            );

        if (this.hasRequirements(route.data.requirements))
            return this.authService.getLoggedInUser().pipe(
                map(user => this.isAllowed(user, route.data.requirements))
            );

        return true;
    }

    private hasRequirements(requirements: AuthRequirements): boolean {
        return requirements !== undefined && requirements !== null && requirements.permissions.length > 0;
    }

    private isAllowed(user: LoggedInUser, requirements: AuthRequirements): boolean {
        return requirements.permissions.every(tp => {
            return user.hasPermission(tp);
        });
    }

    private isAllowedPerKey(user: LoggedInUser, requirements: AuthRequirements, permissionKeyValue: string): boolean {
        return requirements.permissions.some( permission => {
            const hasPermission: boolean = this.isAllowed(user, requirements);
            const hasPermissionPerKey: boolean = user.hasPermissionValueByKey(permission, permissionKeyValue);
            return hasPermission && hasPermissionPerKey;
        });
    }

}

export class AuthRequirements {
    permissions: string[] = [];
    paramPathPermissionPerKey: string;
}

export class AuthRequirementsBuilder {

    private requirements = new AuthRequirements();

    public static new(): AuthRequirementsBuilder {
        return new AuthRequirementsBuilder();
    }

    public permissions(permissions: string[]): AuthRequirementsBuilder {
        this.requirements.permissions = permissions;
        return this;
    }

    public paramPathPermissionPerKey(namePathVariable: string): AuthRequirementsBuilder {
        this.requirements.paramPathPermissionPerKey = namePathVariable;
        return this;
    }

    public build(): AuthRequirements {
        return this.requirements;
    }
}
