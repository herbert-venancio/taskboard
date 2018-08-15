package objective.taskboard.it;

import static objective.taskboard.it.ProjectProfileConfigurationTab.PROJECT_PROFILE_CONFIGURATION_TAG;
import static objective.taskboard.it.components.TabsRouterComponent.TABS_ROUTER_TAG;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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
        tabs = new TabsRouterComponent(driver, cssSelector(TABS_ROUTER_TAG), "Profile");
    }

    public ProjectProfileConfigurationTab selectProfileTab() {
        tabs.select("Profile", PROJECT_PROFILE_CONFIGURATION_TAG);
        return new ProjectProfileConfigurationTab(webDriver, projectInfo).assertTabIsOpen();
    }

    private ProjectAdvancedConfigurationsPage assertPageIsOpen() {
        waitTextInElement(pageTitle, projectInfo.name +" > Advanced Configurations");
        return this;
    }

}
