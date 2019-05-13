package objective.taskboard.followup.cluster;

public class ClusterNotConfiguredException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public ClusterNotConfiguredException() {
        super("No cluster configuration found.");
    }

    public ClusterNotConfiguredException(String message) {
        super(message);
    }
    
    public ClusterNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ClusterNotConfiguredException fromProject(String projectKey) {
        return new ClusterNotConfiguredException(formatMessage(projectKey));
    }
    
    public static ClusterNotConfiguredException fromProject(String projectKey, Throwable cause) {
        return new ClusterNotConfiguredException(formatMessage(projectKey), cause);
    }
    
    private static String formatMessage(String projectKey) {
        return "No cluster configuration found for project " + projectKey + ".";
    }
}