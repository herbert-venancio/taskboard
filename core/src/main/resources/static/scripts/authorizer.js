class Authorizer {

    constructor(permissions) {
        this._permissions = PermissionDto.asList(permissions);
    }

    hasPermission(permissionName, target) {
        const permission = this._permissions.find(permission => permission.name === permissionName);

        if (permission === undefined)
            return false;

        this._validate(permission, target);

        return permission.isAuthorized(target);
    }

    _validate(permission, target) {
        if (permission.applicableTargets === null && target !== undefined)
            throw 'Permission '+ permission.name +' is not authorized for target.';

        else if (permission.applicableTargets !== null && target === undefined)
            throw 'Permission '+ permission.name +' requires target.';

        else if (target !== undefined && !(typeof target === 'string' || target instanceof String))
            throw 'Target must be a string value.';
    }
}

class PermissionDto {
    constructor(permission) {
        this.name = permission.name;
        this.applicableTargets = permission.applicableTargets;
    }

    static asList(permissions) {
        return permissions.map(permission => new PermissionDto(permission));
    }

    isAuthorized(target) {
        return this.applicableTargets === null ? true : this.applicableTargets.some(applicableTarget => applicableTarget === target);
    }
}

class Permission {
    static get TASKBOARD_ADMINISTRATION() { return 'taskboard.administration'; }
    static get PROJECT_ADMINISTRATION() { return 'project.administration'; }
    static get PROJECT_DASHBOARD_VIEW() { return 'project.dashboard.view'; }
    static get PROJECT_DASHBOARD_TACTICAL() { return 'project.dashboard.tactical'; }
    static get PROJECT_DASHBOARD_OPERATIONAL() { return 'project.dashboard.operational'; }
    static get PROJECT_DASHBOARD_CUSTOMER() { return 'project.dashboard.customer' ; }
    static get FOLLOWUP_TEMPLATE_EDIT() { return 'followup.template.edit'; }
    static get SIZING_IMPORT_VIEW() { return 'sizing.import.view'; }
    static get TEAMS_EDIT_VIEW() { return 'teams.edit.view'; }
    static get TEAM_EDIT() { return 'team.edit'; }
}
