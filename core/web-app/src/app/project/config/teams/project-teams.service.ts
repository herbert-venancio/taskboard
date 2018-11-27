import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {retry} from "rxjs/operators";
import {NameableDto} from "../../../shared/model-utils/nameable-dto.model";

@Injectable({
    providedIn: 'root'
})
export class ProjectTeamsService {

    constructor(private http: HttpClient) {}

    getData(projectKey: string): Observable<ProjectTeamsConfigurationDataDto> {
        return this.http.get<ProjectTeamsConfigurationDataDto>(`/ws/project/${projectKey}/default-teams`)
            .pipe(retry(3));
    }

    updateTeams(projectKey: string, defaultTeamId: number, defaultTeamsByIssueType: ProjectTeamByIssueTypeDto[]): Observable<any> {
        return this.http.put(`/ws/project/${projectKey}/default-teams`, {
            defaultTeamId: defaultTeamId,
            defaultTeamsByIssueType: defaultTeamsByIssueType
        });
    }

}

export class ProjectTeamsConfigurationDataDto {
    defaultTeamId: number;
    defaultTeamsByIssueType: ProjectTeamByIssueTypeDto[];
    teams: NameableDto[];
    issueTypes: NameableDto[];
}

export class ProjectTeamByIssueTypeDto {
    id: number;
    issueTypeId: number;
    teamId: number;
    isDisabled: boolean;
}
