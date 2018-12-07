import { MemberDto } from './member-dto.model';

export class TeamDto {
    name: string;
    manager: string;
    members: MemberDto[];
    globallyVisible: boolean;

    public static from(teamControllerData: TeamDto, newMembers: MemberDto[]): TeamDto {
        const team = new TeamDto();
        team.name = teamControllerData.name;
        team.manager = teamControllerData.manager;
        team.globallyVisible = teamControllerData.globallyVisible;
        team.members = teamControllerData.members.concat(newMembers);
        return team;
    }
}
