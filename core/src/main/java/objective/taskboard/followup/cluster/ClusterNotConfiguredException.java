package objective.taskboard.followup.cluster;

public class ClusterNotConfiguredException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public ClusterNotConfiguredException() {
        super();
    }

    public ClusterNotConfiguredException(String message) {
        super(message);
    }

    public static ClusterNotConfiguredException fromProject() {
        return new ClusterNotConfiguredException("No cluster configuration found.");
    }

    public static ClusterNotConfiguredException fromProject(String projectKey) {
        return new ClusterNotConfiguredException("No cluster configuration found for project " + projectKey + ".");
    }
}