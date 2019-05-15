package objective.taskboard.followup;

public class ProjectDatesNotConfiguredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProjectDatesNotConfiguredException() {
        super("The project has no start or delivery date.");
    }

    public ProjectDatesNotConfiguredException(String message) {
        super(message);
    }

    public ProjectDatesNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ProjectDatesNotConfiguredException fromProject(String projectKey) {
        return new ProjectDatesNotConfiguredException(formatMessage(projectKey));
    }

    public static ProjectDatesNotConfiguredException fromProject(String projectKey, Throwable cause) {
        return new ProjectDatesNotConfiguredException(formatMessage(projectKey), cause);
    }
    
    private static String formatMessage(String projectKey) {
        return "The project " + projectKey + " has no start or delivery date.";
    }
}