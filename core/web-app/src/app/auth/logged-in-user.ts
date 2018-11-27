
export class LoggedInUser {
    constructor(
        readonly username: string,
        readonly name: string,
        readonly avatarUrl: string,
        readonly permissions: string[]
    ) {}

    hasPermission(permission: string): boolean {
        return this.permissions && this.permissions.some(tp => tp === permission);
    }
}
