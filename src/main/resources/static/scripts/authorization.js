function Authorizer(permissions) {

    this.hasPermissionInAnyProject = function(permission) {
        if (!permissions)
            return true;

        return permissions.some(function(it) {return it.permissions.indexOf(permission) >= 0});
    }; 

    this.hasPermissionInProject = function(permission, projectKey) {
        if (!permissions)
            return true;

        return permissions.some(function(it) {return it.projectKey == projectKey && it.permissions.indexOf(permission) >= 0});
    };
}