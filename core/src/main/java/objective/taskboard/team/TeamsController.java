package objective.taskboard.team;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.TeamsEditViewPermission;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;

@RestController
@RequestMapping("/ws/teams")
class TeamsController {

    private UserTeamService userTeamService;
    private TeamsEditViewPermission teamsEditViewPermission;

    @Autowired
    public TeamsController(
            UserTeamService userTeamService,
            TeamsEditViewPermission teamsEditViewPermission) {
        this.userTeamService = userTeamService;
        this.teamsEditViewPermission = teamsEditViewPermission;
    }

    @GetMapping
    public ResponseEntity<?> getTeams() {
        if (!teamsEditViewPermission.isAuthorized())
            return new ResponseEntity<>(NOT_FOUND);

        return new ResponseEntity<>(TeamDto.from(userTeamService.getTeamsThatUserCanAdmin()), OK);
    }

    @GetMapping(value="{teamName}")
    public ResponseEntity<?> getTeam(@PathVariable String teamName) {
        if (!teamsEditViewPermission.isAuthorized())
            return new ResponseEntity<>(NOT_FOUND);

        Optional<Team> team = findTeam(teamName);
        if (!team.isPresent())
            return new ResponseEntity<>("Team \""+ teamName +"\" not found.", NOT_FOUND);

        return new ResponseEntity<>(TeamDto.from(team.get()), OK);
    }

    @PutMapping(value="{teamName}")
    @Transactional
    public ResponseEntity<?> updateTeam(@PathVariable String teamName, @RequestBody TeamDto teamDto) {
        if (!teamsEditViewPermission.isAuthorized())
            return new ResponseEntity<>(NOT_FOUND);

        List<String> errors = validateTeam(teamDto);
        if (!errors.isEmpty())
            return new ResponseEntity<>(errors, BAD_REQUEST);

        Optional<Team> team = findTeam(teamName);
        if (!team.isPresent())
            return new ResponseEntity<>("Team \""+ teamName +"\" not found.", NOT_FOUND);

        updateTeam(teamDto, team.get());

        return new ResponseEntity<>(OK);
    }

    private List<String> validateTeam(TeamDto teamData) {
        List<String> errors = new ArrayList<>();

        if (isEmpty(teamData.name))
            errors.add("\"teamName\" is required.");

        if (isEmpty(teamData.manager))
            errors.add("\"manager\" is required.");

        boolean hasEmptyMember = teamData.members.stream().anyMatch(member -> isEmpty(member.name));
        if (hasEmptyMember)
            errors.add("Empty member isn't allowed.");

        Set<String> teamMemberUnique = new HashSet<>();
        teamData.members.stream()
            .filter(member -> !isEmpty(member.name) && !teamMemberUnique.add(member.name))
            .forEach(member -> errors.add("Member \"" + member.name + "\" repeated."));

        return errors.stream()
                .distinct()
                .sorted()
                .collect(toList());
    }

    private Optional<Team> findTeam(String teamName) {
        return userTeamService.getTeamsThatUserCanAdmin().stream()
            .filter(team -> team.getName().equals(teamName))
            .findFirst();
    }

    private void updateTeam(TeamDto data, Team team) {
        team.setName(data.name);
        team.setManager(data.manager);
        team.setGloballyVisible(data.globallyVisible);

        Stream<UserTeam> membersToRemove = team.getMembers().stream()
            .filter(userTeam -> data.members.stream().noneMatch(member -> member.name.equals(userTeam.getUserName())));
        membersToRemove.forEach(userTeam -> team.getMembers().remove(userTeam));

        Stream<UserTeam> membersToAdd = data.members.stream()
            .filter(member -> team.getMembers().stream().noneMatch(userTeam -> userTeam.getUserName().equals(member.name)))
            .map(member -> new UserTeam(member.name, data.name, member.role));
        membersToAdd.forEach(userTeam -> team.getMembers().add(userTeam));

        Stream<MemberDto> membersToUpdate = data.members.stream()
            .filter(member -> team.getMembers().stream().noneMatch(userTeam -> {
                return userTeam.getUserName().equals(member.name) && userTeam.getRole() == member.role; }));
        membersToUpdate.forEach(member -> {
            UserTeam userTeam = team.getMembers().stream().filter(user -> user.getUserName().equals(member.name)).findFirst().get();
            userTeam.setRole(member.role);
        });

        userTeamService.saveTeam(team);
    }

    static class TeamDto {
        public String name;
        public String manager;
        public boolean globallyVisible;
        public List<MemberDto> members = new ArrayList<MemberDto>();

        public static TeamDto from(Team team) {
            TeamDto dto = new TeamDto();
            dto.name = team.getName();
            dto.manager = team.getManager();
            dto.globallyVisible = team.isGloballyVisible();
            team.getMembers().stream()
                .sorted((a,b) -> a.getUserName().compareTo(b.getUserName()))
                .forEach(member -> dto.members.add(new MemberDto(member.getUserName(), member.getRole())));
            return dto;
        }

        public static List<TeamDto> from(Set<Team> teams) {
            return teams.stream()
                    .map(team -> TeamDto.from(team))
                    .sorted((a,b) -> a.name.compareTo(b.name))
                    .collect(toList());
        }

    }

    static class MemberDto{
        public String name;

        public UserTeamRole role;

        public MemberDto(){}

        public MemberDto(String name, UserTeamRole role) {
            this.name = name;
            this.role = role;
        }
    }

}
