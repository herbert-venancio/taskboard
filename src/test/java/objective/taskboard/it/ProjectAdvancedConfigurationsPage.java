package objective.taskboard.it;

import static objective.taskboard.it.ProjectProfileConfigurationTab.PROJECT_PROFILE_CONFIGURATION_TAG;
import static objective.taskboard.it.ProjectClusterConfiguration.PROJECT_CLUSTER_CONFIGURATION_TAG;
import static objective.taskboard.it.ProjectDefaultTeamsConfiguration.PROJECT_TEAMS_CONFIGURATION_TAG;
import static objective.taskboard.it.components.TabsRouterComponent.TABS_ROUTER_TAG;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.components.TabsRouterComponent;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectAdvancedConfigurationsPage extends AbstractUiFragment {

    private static final String PROFILE_TAB_NAME = "Profile";
    private static final String TEAMS_TAB_NAME = "Teams";
    private static final String CLUSTER_TAB_NAME = "Cluster";

    @FindBy(id="tb-page-title")
    private WebElement pageTitle;

    private TabsRouterComponent tabs;

    private ProjectInfo projectInfo;

    public ProjectAdvancedConfigurationsPage(WebDriver driver, ProjectInfo projectInfo) {
        super(driver);
        this.projectInfo = projectInfo;
        initElements(driver, this);
        assertPageIsOpen();
        tabs = new TabsRouterComponent(driver, cssSelector(TABS_ROUTER_TAG), PROFILE_TAB_NAME, TEAMS_TAB_NAME, CLUSTER_TAB_NAME);
    }

    public ProjectProfileConfigurationTab selectProfileConfiguration() {
        tabs.select(PROFILE_TAB_NAME, PROJECT_PROFILE_CONFIGURATION_TAG, false);
        return new ProjectProfileConfigurationTab(webDriver, projectInfo).assertTabIsOpen();
    }

    public ProjectProfileConfigurationTab selectProfileConfigurationWithLeaveConfirmation() {
        tabs.select(PROFILE_TAB_NAME, PROJECT_PROFILE_CONFIGURATION_TAG, true);
        return new ProjectProfileConfigurationTab(webDriver, projectInfo).assertTabIsOpen();
    }

    public void selectProfileConfigurationButStay() {
        tabs.selectButStay(PROFILE_TAB_NAME);
    }

    public ProjectDefaultTeamsConfiguration selectTeamsConfiguration() {
        tabs.select(TEAMS_TAB_NAME, PROJECT_TEAMS_CONFIGURATION_TAG, false);
        return new ProjectDefaultTeamsConfiguration(webDriver, projectInfo).assertTabIsOpen();
    }

    public ProjectClusterConfiguration selectClusterConfiguration() {
        tabs.select(CLUSTER_TAB_NAME, PROJECT_CLUSTER_CONFIGURATION_TAG, false);
        return new ProjectClusterConfiguration(webDriver, projectInfo).assertTabIsOpen();
    }

    private ProjectAdvancedConfigurationsPage assertPageIsOpen() {
        waitTextInElement(pageTitle, projectInfo.name +" > Advanced Configurations");
        return this;
    }

}
