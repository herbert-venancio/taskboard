package objective.taskboard.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import objective.taskboard.auth.Authorizer;
import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.Authorizer.ProjectPermissionsData;
import objective.taskboard.cycletime.HolidayService;
import objective.taskboard.data.Team;
import objective.taskboard.data.User;
import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.google.GoogleApiConfig;
import objective.taskboard.jira.FieldMetadataService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.client.JiraFieldDataDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.team.UserTeamService;

@Controller
public class HomeController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private JiraProperties jiraPropeties;

    @Autowired
    private GoogleApiConfig googleApiConfig;

    @Autowired
    private Authorizer authorizer;

    @Autowired
    private FieldMetadataService fieldMetadataService;

    @Autowired
    private UserTeamService userTeamService;

    @Autowired
    private FollowUpFacade followupFacade;

    @Autowired
    private LoggedUserDetails loggedInUser;

    @RequestMapping("/")
    public String home(Model model) {
        User user = jiraService.getLoggedUser();
        model.addAttribute("user", serialize(user));
        model.addAttribute("jiraCustomfields", jiraPropeties.getCustomfield());
        model.addAttribute("jiraIssuetypes", jiraPropeties.getIssuetype());
        model.addAttribute("jiraStatusesCompletedIds", serialize(jiraPropeties.getStatusesCompletedIds()));
        model.addAttribute("jiraStatusesCanceledIds", serialize(jiraPropeties.getStatusesCanceledIds()));
        model.addAttribute("jiraTransitionsWithRequiredCommentNames", serialize(jiraPropeties.getTransitionsWithRequiredCommentNames()));
        model.addAttribute("holidays", serialize(holidayService.getHolidays()));
        model.addAttribute("googleClientId", googleApiConfig.getClientId());
        model.addAttribute("permissions", serialize(new PermissionsDto(loggedInUser.isAdmin(), authorizer.getProjectsPermission())));
        model.addAttribute("fieldNames", getFieldNames());
        
        Set<Team> teamsVisibleToUser = userTeamService.getTeamsVisibleToLoggedInUser();
        model.addAttribute("teams", serialize(
                teamsVisibleToUser.stream()
                    .map(t->new TeamControllerData(t))
                    .sorted((a,b)->a.teamName.compareTo(b.teamName))
                    .collect(Collectors.toList())));

        model.addAttribute("hasFollowupTemplateAvailable", followupFacade.getTemplatesForCurrentUser().size() > 0);

        return "index";
    }

    private String serialize(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getFieldNames() {
        return fieldMetadataService.getFieldsMetadataAsUser()
                .stream()
                .collect(Collectors.toMap(JiraFieldDataDto::getId, JiraFieldDataDto::getName));
    }

    static class PermissionsDto {
        public boolean isAdmin;
        public List<ProjectPermissionsData> projectsPermissions;

        private PermissionsDto(boolean isAdmin, List<ProjectPermissionsData> projectsPermissions) {
            this.isAdmin = isAdmin;
            this.projectsPermissions = projectsPermissions;
        }
    }
}