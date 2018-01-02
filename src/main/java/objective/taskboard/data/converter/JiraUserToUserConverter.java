package objective.taskboard.data.converter;

import objective.taskboard.data.User;
import objective.taskboard.data.UserTeam;
import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.repository.UserTeamCachedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JiraUserToUserConverter implements Converter<JiraUser, User> {

    @Autowired
    private UserTeamCachedRepository userTeamRepo;

    @Override
    public User convert(JiraUser source) {
        return new User(source.displayName, source.name, source.emailAddress, source.getAvatarUri(), isCustomer(source));
    }

    private boolean isCustomer(JiraUser jiraUser) {
        List<UserTeam> teams = userTeamRepo.findByUserName(jiraUser.name);
        return teams.isEmpty() ||
                teams.stream()
                .anyMatch(userTeam -> userTeam.getTeam().endsWith("_CUSTOMER"));
    }
}
