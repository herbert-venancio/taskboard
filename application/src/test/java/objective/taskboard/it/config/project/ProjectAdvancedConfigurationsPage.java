package objective.taskboard.it.config.project;

import static objective.taskboard.it.config.project.ProjectChangeRequestConfigurationTab.CHANGE_REQUEST_TAB_NAME;
import static objective.taskboard.it.config.project.ProjectChangeRequestConfigurationTab.PROJECT_CHANGE_REQUEST_CONFIGURATION_TAG;
import static objective.taskboard.it.config.project.ProjectClusterConfiguration.CLUSTER_TAB_NAME;
import static objective.taskboard.it.config.project.ProjectClusterConfiguration.PROJECT_CLUSTER_CONFIGURATION_TAG;
import static objective.taskboard.it.config.project.ProjectDefaultTeamsConfiguration.PROJECT_TEAMS_CONFIGURATION_TAG;
import static objective.taskboard.it.config.project.ProjectDefaultTeamsConfiguration.TEAMS_TAB_NAME;
import static objective.taskboard.it.config.project.ProjectProfileConfigurationTab.PROFILE_TAB_NAME;
import static objective.taskboard.it.config.project.ProjectProfileConfigurationTab.PROJECT_PROFILE_CONFIGURATION_TAG;
import static objective.taskboard.it.components.TabsRouterComponent.TABS_ROUTER_TAG;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.AbstractUiFragment;
import objective.taskboard.it.components.TabsRouterComponent;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectAdvancedConfigurationsPage extends AbstractUiFragment {

    @FindBy(id="tb-page-title")
    private WebElement pageTitle;

    private TabsRouterComponent tabs;

    private ProjectInfo projectInfo;

    public ProjectAdvancedConfigurationsPage(WebDriver driver, ProjectInfo projectInfo) {
        super(driver);
        this.projectInfo = projectInfo;
        initElements(driver, this);
        assertPageIsOpen();
        tabs = new TabsRouterComponent(driver, cssSelector(TABS_ROUTER_TAG));
        tabs.addTab(PROFILE_TAB_NAME, PROJECT_PROFILE_CONFIGURATION_TAG, ProjectProfileConfigurationTab.factory(projectInfo));
        tabs.addTab(TEAMS_TAB_NAME  , PROJECT_TEAMS_CONFIGURATION_TAG  , ProjectDefaultTeamsConfiguration.factory(projectInfo));
        tabs.addTab(CLUSTER_TAB_NAME, PROJECT_CLUSTER_CONFIGURATION_TAG, ProjectClusterConfiguration.factory(projectInfo));
        tabs.addTab(CHANGE_REQUEST_TAB_NAME, PROJECT_CHANGE_REQUEST_CONFIGURATION_TAG, ProjectChangeRequestConfigurationTab.factory(projectInfo));
        tabs.waitAllTabsExists();
    }

    public ProjectProfileConfigurationTab selectProfileConfiguration() {
        return tabs.<ProjectProfileConfigurationTab>selectTab(PROFILE_TAB_NAME).waitLoaded();
    }

    public ProjectDefaultTeamsConfiguration selectTeamsConfiguration() {
        return tabs.<ProjectDefaultTeamsConfiguration>selectTab(TEAMS_TAB_NAME).waitLoaded();
    }

    public ProjectClusterConfiguration selectClusterConfiguration() {
        return tabs.<ProjectClusterConfiguration>selectTab(CLUSTER_TAB_NAME).waitLoaded();
    }

    public ProjectChangeRequestConfigurationTab selectChangeRequestConfiguration() {
        return tabs.<ProjectChangeRequestConfigurationTab>selectTab(CHANGE_REQUEST_TAB_NAME).waitLoaded();
    }

    private ProjectAdvancedConfigurationsPage assertPageIsOpen() {
        waitTextInElement(pageTitle, projectInfo.name +" > Advanced Configurations");
        return this;
    }
}
