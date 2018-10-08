package objective.taskboard.it;

import static java.lang.String.format;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import objective.taskboard.it.components.ButtonComponent;
import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectClusterConfiguration extends AbstractUiFragment {

    public static final String PROJECT_CLUSTER_CONFIGURATION_TAG = "tb-project-cluster";

    private ButtonComponent save;
    private SnackBarComponent snackbar;
    private ProjectAdvancedConfigurationsPage projectAdvancedPage;

    public ProjectClusterConfiguration(WebDriver driver, ProjectInfo projectInfo) {
        super(driver);
        this.save = new ButtonComponent(driver, cssSelector(PROJECT_CLUSTER_CONFIGURATION_TAG + " #tb-project-cluster-save"));
        this.snackbar = new SnackBarComponent(webDriver, cssSelector(SNACK_BAR_TAG + "#tb-project-cluster-snackbar"));
        this.projectAdvancedPage = new ProjectAdvancedConfigurationsPage(driver, projectInfo);
    }

    public ProjectClusterConfiguration assertTabIsOpen() {
        waitUntilElementExists(tagName(PROJECT_CLUSTER_CONFIGURATION_TAG));
        return this;
    }

    public ProjectClusterConfiguration assertEffort(String issueType, String size, String expectedEffort) {
        WebElement input = getInputByIssueTypeAndSize(issueType, "effort-" + size);
        waitAttributeValueInElement(input, "value", expectedEffort);
        return this;
    }

    public ProjectClusterConfiguration setEffort(String issueType, String size, String effort) {
        WebElement input = getInputByIssueTypeAndSize(issueType, "effort-" + size);
        setInputValue(input, effort);
        return this;
    }

    public ProjectClusterConfiguration assertCycle(String issueType, String size, String expectedCycle) {
        WebElement input = getInputByIssueTypeAndSize(issueType, "cycle-" + size);
        waitAttributeValueInElement(input, "value", expectedCycle);
        return this;
    }

    public ProjectClusterConfiguration setCycle(String issueType, String size, String cycle) {
        WebElement input = getInputByIssueTypeAndSize(issueType, "cycle-" + size);
        setInputValue(input, cycle);
        return this;
    }

    private WebElement getInputByIssueTypeAndSize(String issueType, String inputName) {
        By inputSelector = cssSelector(format("obj-expansion-panel[data-issue-type=\"%s\"] input[name=\"%s\"]", issueType, inputName));
        return getElementWhenItExists(inputSelector);
    }

    public ProjectClusterConfiguration assertIsFromBaseCluster(String issueType, String size, Boolean isVisible) {
        By spanSelector = cssSelector(format("obj-expansion-panel[data-issue-type=\"%s\"] th[data-sizing=\"%s\"] .from-base-cluster", issueType, size));
        waitElementExistenceAndVisibilityIs(isVisible, spanSelector);
        return this;
    }

    public ProjectClusterConfiguration assertSaveDisabled(boolean disabled) {
        save.waitDisabledBe(disabled);
        return this;
    }

    public ProjectClusterConfiguration save() {
        save.click();
        return this;
    }

    public ProjectClusterConfiguration assertSnackbarSavedIsOpen() {
        snackbar.waitTitleToBe("Project cluster saved");
        return this;
    }

    public ProjectClusterConfiguration assertSnackbarErrorIsOpen() {
        snackbar.waitTitleToBe("Please review the form");
        return this;
    }

    public ProjectClusterConfiguration refresh() {
        webDriver.navigate().refresh();
        assertTabIsOpen();
        return this;
    }

    public ProjectClusterConfiguration tryToGoToProfileConfigurationButStay() {
        projectAdvancedPage.selectProfileConfigurationButStay();
        return this;
    }

    public ProjectProfileConfigurationTab goToProfileConfigurationWithoutLeaveConfirmation() {
        return projectAdvancedPage.selectProfileConfiguration();
    }

    public ProjectProfileConfigurationTab goToProfileConfigurationWithLeaveConfirmation() {
        return projectAdvancedPage.selectProfileConfigurationWithLeaveConfirmation();
    }
}
