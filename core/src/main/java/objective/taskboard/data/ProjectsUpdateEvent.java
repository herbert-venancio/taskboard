package objective.taskboard.data;

import org.springframework.context.ApplicationEvent;

public class ProjectsUpdateEvent extends ApplicationEvent {
    private static final long serialVersionUID = 2760956870188778965L;

    public final String[] projects;

    public ProjectsUpdateEvent(Object source, String[] projects) {
        super(source);
        this.projects = projects;
    }
}
