package objective.taskboard.jira;

import org.springframework.context.ApplicationEvent;

public class ProjectUpdateEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String projectKey;

    public ProjectUpdateEvent(Object source, String projectKey) {
        super(source);
        this.projectKey = projectKey;
    }

    public String getProjectKey() {
        return projectKey;
    }

}
