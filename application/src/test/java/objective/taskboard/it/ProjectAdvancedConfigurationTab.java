package objective.taskboard.it;

import objective.taskboard.it.components.TabComponent;
import objective.taskboard.it.components.TabsRouterComponent;
import objective.taskboard.testUtils.ProjectInfo;
import org.openqa.selenium.WebDriver;

import static org.openqa.selenium.By.tagName;

public abstract class ProjectAdvancedConfigurationTab<T extends ProjectAdvancedConfigurationTab<T>> extends AbstractUiFragment {
    protected final TabComponent<T> tab;
    protected final ProjectInfo projectInfo;

    public ProjectAdvancedConfigurationTab(WebDriver driver, TabComponent<T> tab, ProjectInfo projectInfo) {
        super(driver);
        this.tab = tab;
        this.projectInfo = projectInfo;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T assertTabIsOpen() {
        waitUntilElementExists(tagName(tab.componentName));
        return self();
    }

    public T refresh() {
        webDriver.navigate().refresh();
        tab.waitIsSelected();
        return self();
    }

    public TabsRouterComponent.SelectTab<T, ProjectProfileConfigurationTab> selectProfileTab() {
        return tab.selectTab(ProjectProfileConfigurationTab.PROFILE_TAB_NAME);
    }

    public TabsRouterComponent.SelectTab<T, ProjectDefaultTeamsConfiguration> selectTeamsTab() {
        return tab.selectTab(ProjectDefaultTeamsConfiguration.TEAMS_TAB_NAME);
    }

    public TabsRouterComponent.SelectTab<T, ProjectClusterConfiguration> selectClusterTab() {
        return tab.selectTab(ProjectClusterConfiguration.CLUSTER_TAB_NAME);
    }

    public ProjectProfileConfigurationTab goToProfileConfiguration() {
        return selectProfileTab().expectNoConfirmation();
    }

    public ProjectDefaultTeamsConfiguration goToTeamsConfiguration() {
        return selectTeamsTab().expectNoConfirmation();
    }

    public ProjectClusterConfiguration goToClusterConfiguration() {
        return selectClusterTab().expectNoConfirmation();
    }
}
