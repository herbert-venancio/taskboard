package objective.taskboard.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.Clock;

@Service
public class TaskboardUserService {

    private final JiraProperties jiraProperties;
    private final TaskboardUserRepository taskboardUserRepository;
    private final Clock clock;

    @Autowired
    public TaskboardUserService(
            JiraProperties jiraProperties,
            TaskboardUserRepository taskboardUserRepository,
            Clock clock) {
        this.jiraProperties = jiraProperties;
        this.taskboardUserRepository = taskboardUserRepository;
        this.clock = clock;
    }

    @Transactional
    public TaskboardUser getTaskboardUser(String username) {
        return taskboardUserRepository.getByUsername(username)
                .orElseGet(() -> createTaskboardUser(username));
    }

    private TaskboardUser createTaskboardUser(String username) {
        TaskboardUser user = new TaskboardUser(username);

        if (username.equals(jiraProperties.getLousa().getUsername()))
            user.setAdmin(true);

        taskboardUserRepository.add(user);
        return user;
    }

    @Transactional
    public void updateLastLoginToNow(String username) {
        TaskboardUser user = getTaskboardUser(username);
        user.setLastLogin(clock.now());
    }

}
