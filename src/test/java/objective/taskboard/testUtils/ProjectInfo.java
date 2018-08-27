package objective.taskboard.testUtils;

public enum ProjectInfo {

    TASKB("TASKB", "Taskboard"),
    PROJ1("PROJ1", "Project 1"),
    PROJ2("PROJ2", "Project 2");

    public final String key;
    public final String name;

    private ProjectInfo(String projectKey, String projectName) {
        this.key = projectKey;
        this.name = projectName;
    }

}
