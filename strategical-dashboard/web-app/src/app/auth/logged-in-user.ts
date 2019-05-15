
export class LoggedInUser {
    constructor(
        readonly username: string,
        readonly name: string,
        readonly avatarUrl: string,
        readonly permissions: string[],
        readonly permissionsPerKey: { [permission: string]: string[]; }
    ) {}

    hasPermission(permission: string): boolean {
        return this.permissions && this.permissions.some(tp => tp === permission);
    }

    hasPermissionValueByKey(permissionKey: string, permissionValue: string): boolean {

        if (this.permissionsPerKey) {
            const values: string[] = this.permissionsPerKey[permissionKey];
            if (values)
                return values.filter(item => item === permissionValue) && true;
        }
    }
}
