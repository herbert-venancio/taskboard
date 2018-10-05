
export class LoggedInUser {
    constructor(
        readonly username: string,
        readonly name: string,
        readonly avatarUrl: string,
        readonly isAdmin: boolean
    ) {
    }
}
