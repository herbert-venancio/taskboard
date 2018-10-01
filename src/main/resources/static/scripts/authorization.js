function Authorizer(permissions) {

    this.permissions = {
        DASHBOARD_TACTICAL: 'dashboard.tactical',
        DASHBOARD_OPERATIONAL: 'dashboard.operational',
        ADMINISTRATIVE: 'taskboard.administrative'
    };

    this.isAdmin = function() {
        return permissions.isAdmin;
    };

    this.hasPermissionInAnyProject = function(permission) {
        if (!permissions)
            return true;

        return permissions.projectsPermissions.some(function(project) {return project.permissions.indexOf(permission) >= 0});
    }; 

    this.hasPermissionInProject = function(permission, projectKey) {
        if (!permissions.projectsPermissions)
            return true;

        return permissions.projectsPermissions.some(function(project) {return project.projectKey === projectKey && project.permissions.indexOf(permission) >= 0});
    };
}