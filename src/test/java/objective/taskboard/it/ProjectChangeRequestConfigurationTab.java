package objective.taskboard.it;

import static java.util.stream.Collectors.joining;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.apache.commons.lang3.StringUtils.join;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.it.components.TabComponent;
import objective.taskboard.it.components.TabsRouterComponent;
import objective.taskboard.testUtils.ProjectInfo;

public class ProjectChangeRequestConfigurationTab extends ProjectAdvancedConfigurationTab<ProjectChangeRequestConfigurationTab> {

    public static final String CHANGE_REQUEST_TAB_NAME = "Change Requests";
    public static final String PROJECT_CHANGE_REQUEST_CONFIGURATION_TAG = "tb-project-changerequests";

    @FindBy(id="tb-project-changeRequests-add-item")
    private WebElement addItemButton;

    @FindBy(id="tb-project-changeRequests-back-to-project")
    private WebElement backToProjectButton;
    
    @FindBy(id="tb-project-changeRequests-save")
    private WebElement saveButton;

    @FindBy(id="tb-project-changeRequests-items")
    private WebElement itemsTable;

    private SnackBarComponent snackbar;

    public static TabsRouterComponent.TabFactory<ProjectChangeRequestConfigurationTab> factory(ProjectInfo projectInfo) {
        return (webDriver, tab) -> new ProjectChangeRequestConfigurationTab(webDriver, tab, projectInfo);
    }

    public ProjectChangeRequestConfigurationTab(WebDriver driver, TabComponent<ProjectChangeRequestConfigurationTab> tab, ProjectInfo projectInfo) {
        super(driver, tab, projectInfo);
        initElements(driver, this);
        this.snackbar = new SnackBarComponent(webDriver, cssSelector(SNACK_BAR_TAG + "#tb-project-changeRequests-snackbar"));
    }

    public ProjectConfigurationDialog backToProject() {
        waitForClick(backToProjectButton);
        return new ProjectConfigurationDialog(webDriver, projectInfo).assertIsOpen();
    }

    public ProjectChangeRequestConfigurationTab save() {
        waitForClick(saveButton);
        return this;
    }

    public ProjectChangeRequestConfigurationTab addItem() {
        waitForClick(addItemButton);
        return this;
    }

    public ProjectChangeRequestConfigurationTab setName(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='name']"), value);
        return this;
    }

    public ProjectChangeRequestConfigurationTab setBudgetIncrease(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='budgetIncrease']"), value);
        return this;
    }

    public ProjectChangeRequestConfigurationTab setDate(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='date']"), value);
        return this;
    }

    private WebElement selectElementInsideRow(int rowIndex, String elementSelector) {
        By rowElementSelector = By.cssSelector("tbody tr:nth-of-type(" + (rowIndex + 1) + ") " + elementSelector);
        return getChildElementWhenExists(itemsTable, rowElementSelector);
    }

    public ProjectChangeRequestConfigurationTab assertSnackbarSavedIsOpen() {
        snackbar.waitTitleToBe("Project change requests saved");
        return this;
    }

    public ProjectChangeRequestConfigurationTab assertItems(String... expectedRows) {
        waitAssertEquals(join(expectedRows, "\n"), () -> {
            return itemsTable.findElements(By.cssSelector("tbody tr")).stream()
                    .map(row -> row.findElements(By.tagName("input")).stream().map(e -> e.getAttribute("value")).collect(joining(" | ")))
                    .collect(joining("\n"));
        });

        return this;
    }

    public ProjectChangeRequestConfigurationTab remove(int i) {
        WebElement removeButton = selectElementInsideRow(i, ".remove-button");
        waitForClick(removeButton);
        return this;
    }
    

}
