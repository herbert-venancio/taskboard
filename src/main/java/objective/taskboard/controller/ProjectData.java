package objective.taskboard.controller;

import java.util.HashSet;
import java.util.Set;

import objective.taskboard.domain.ProjectTeam;

public class ProjectData {
    public String projectKey;
    public Set<String> teams = new HashSet<>();
    
    public ProjectData() {}
    public ProjectData(ProjectTeam project) {
        this.projectKey = project.getProjectKey();
    }
}
