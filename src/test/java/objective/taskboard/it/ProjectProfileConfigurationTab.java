package objective.taskboard.it;

import static java.util.stream.Collectors.joining;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.apache.commons.lang3.StringUtils.join;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectProfileConfigurationTab extends AbstractUiFragment {

    public static final String PROJECT_PROFILE_CONFIGURATION_TAG = "tb-project-profile";

    @FindBy(id="tb-project-profile-add-item")
    private WebElement addItemButton;

    @FindBy(id="tb-project-profile-back-to-project")
    private WebElement backToProjectButton;
    
    @FindBy(id="tb-project-profile-save")
    private WebElement saveButton;
    
    @FindBy(id="tb-project-profile-items")
    private WebElement itemsTable;
    
    private SnackBarComponent snackbar;
    private ProjectAdvancedConfigurationsPage projectAdvancedPage;
    private ProjectInfo projectInfo;

    public ProjectProfileConfigurationTab(WebDriver driver, ProjectInfo projectInfo) {
        super(driver);
        initElements(driver, this);
        this.snackbar = new SnackBarComponent(driver, By.cssSelector(SNACK_BAR_TAG +"#tb-project-profile-snackbar"));
        this.projectAdvancedPage = new ProjectAdvancedConfigurationsPage(driver, projectInfo);
        this.projectInfo = projectInfo;
    }

    public ProjectProfileConfigurationTab assertTabIsOpen() {
        waitUntilElementExists(By.tagName(PROJECT_PROFILE_CONFIGURATION_TAG));
        return this;
    }

    public ProjectConfigurationDialog backToProject() {
        waitForClick(backToProjectButton);
        return new ProjectConfigurationDialog(webDriver, projectInfo).assertIsOpen();
    }

    public ProjectProfileConfigurationTab refresh() {
        webDriver.navigate().refresh();
        assertTabIsOpen();
        return this;
    }
    
    public ProjectProfileConfigurationTab save() {
        waitForClick(saveButton);
        return this;
    }
    
    public ProjectProfileConfigurationTab addItem() {
        waitForClick(addItemButton);
        return this;
    }
    
    public ProjectProfileConfigurationTab setRoleName(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='roleName']"), value);
        return this;
    }
    
    public ProjectProfileConfigurationTab setPeopleCount(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='peopleCount']"), value);
        return this;
    }
    
    public ProjectProfileConfigurationTab setAllocationStart(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='allocationStart']"), value);
        return this;
    }
    
    public ProjectProfileConfigurationTab setAllocationEnd(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='allocationEnd']"), value);
        return this;
    }

    private WebElement selectElementInsideRow(int rowIndex, String elementSelector) {
        By rowElementSelector = By.cssSelector("tbody tr:nth-of-type(" + (rowIndex + 1) + ") " + elementSelector);
        return getChildElementWhenExists(itemsTable, rowElementSelector);
    }
    
    public ProjectProfileConfigurationTab assertSnackbarSavedIsOpen() {
        snackbar.waitTitleToBe("Project profile saved");
        return this;
    }

    public ProjectProfileConfigurationTab assertItems(String... expectedRows) {
        waitAssertEquals(join(expectedRows, "\n"), () -> {
            return itemsTable.findElements(By.cssSelector("tbody tr")).stream()
                    .map(row -> row.findElements(By.tagName("input")).stream().map(e -> e.getAttribute("value")).collect(joining(" | ")))
                    .collect(joining("\n"));
        });

        return this;
    }

    public ProjectProfileConfigurationTab remove(int i) {
        WebElement removeButton = selectElementInsideRow(i, ".remove-button");
        waitForClick(removeButton);
        return this;
    }

    public ProjectClusterConfiguration goToClusterConfiguration() {
        return projectAdvancedPage.selectClusterConfiguration();
    }
}
