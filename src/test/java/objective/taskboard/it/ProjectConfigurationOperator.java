package objective.taskboard.it;

import objective.taskboard.testUtils.ProjectInfo;

public class ProjectConfigurationOperator {

    public static ProjectConfigurationDialog openFromMainMenu(MainPage mainPage, ProjectInfo projectInfo) {
        return mainPage.openMenuFilters()
            .openProjectsConfiguration()
            .openProjectConfigurationModal(projectInfo);
    }

}
