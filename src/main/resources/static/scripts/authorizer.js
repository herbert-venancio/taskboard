class Authorizer {

    constructor(permissions) {
        this._permissions = PermissionDto.asList(permissions);
    }

    hasPermission(permissionName, target) {
        const permission = this._permissions.find(permission => permission.name === permissionName);

        if (permission === undefined)
            return false;

        this._validate(permission, target);

        return permission.accepts(target);
    }

    _validate(permission, target) {
        if (permission.applicableTargets === null && target !== undefined)
            throw 'Permission '+ permission.name +' doesn\'t accepts target.';

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

    accepts(target) {
        return this.applicableTargets === null ? true : this.applicableTargets.some(applicableTarget => applicableTarget === target);
    }
}

class Permission {
    static get TASKBOARD_ADMINISTRATION() { return 'taskboard.administration'; }
    static get PROJECT_DASHBOARD_TACTICAL() { return 'project.dashboard.tactical'; }
    static get PROJECT_DASHBOARD_TACTICAL_VIEW() { return 'project.dashboard.tactical.view'; }
    static get PROJECT_DASHBOARD_OPERATIONAL() { return 'project.dashboard.operational'; }
    static get PROJECT_DASHBOARD_OPERATIONAL_VIEW() { return 'project.dashboard.operational.view'; }
    static get PROJECT_ADMINISTRATION() { return 'project.administration'; }
    static get PROJECT_ADMINISTRATION_VIEW() { return 'project.administration.view'; }
}