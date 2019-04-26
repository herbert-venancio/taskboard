package objective.taskboard.followup;

public class ProjectDatesNotConfiguredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProjectDatesNotConfiguredException() {
        super();
    }

    public ProjectDatesNotConfiguredException(String message) {
        super(message);
    }

    public static ProjectDatesNotConfiguredException fromProject() {
        return new ProjectDatesNotConfiguredException("The project has no start or delivery date.");
    }

    public static ProjectDatesNotConfiguredException fromProject(String projectKey) {
        return new ProjectDatesNotConfiguredException("The project " + projectKey + " has no start or delivery date.");
    }
}