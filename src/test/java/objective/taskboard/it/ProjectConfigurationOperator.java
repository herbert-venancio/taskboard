package objective.taskboard.it;

public class ProjectConfigurationOperator {

    public static ProjectConfigurationDialog openFromMainMenu(MainPage mainPage, String projectKey) {
        return mainPage.openMenuFilters()
            .openProjectsConfiguration()
            .openProjectConfigurationModal(projectKey);
    }
}
