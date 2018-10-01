export class TeamDto {
    name: string;
    manager: string;
    members: string[];

    public static from(teamControllerData: TeamDto, newMembers: string[]): TeamDto {
        const team = new TeamDto();
        team.name = teamControllerData.name;
        team.manager = teamControllerData.manager;
        team.members = teamControllerData.members.concat(newMembers);
        return team;
    }
}
