package objective.taskboard.it;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.join;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProjectProfileConfigPage extends AbstractUiFragment {

    @FindBy(id="tb-page-title")
    private WebElement pageTitle;
    
    @FindBy(id="tb-project-profile-add-item")
    private WebElement addItemButton;

    @FindBy(id="tb-project-profile-back-to-project")
    private WebElement backToProjectButton;
    
    @FindBy(id="tb-project-profile-save")
    private WebElement saveButton;
    
    @FindBy(id="tb-project-profile-items")
    private WebElement itemsTable;
    
    @FindBy(css="#tb-project-profile-snackbar .title")
    private WebElement snackabarTitle;

    public ProjectProfileConfigPage(WebDriver driver) {
        super(driver);
        initElements(driver, this);
    }

    public ProjectProfileConfigPage assertPageIsOpen() {
        waitTextInElement(pageTitle, "Taskboard > Project Profile");
        return this;
    }
    
    public ProjectProfileConfigPage backToProject() {
        waitForClick(backToProjectButton);
        return this;
    }
    
    public ProjectProfileConfigPage refresh() {
        webDriver.navigate().refresh();
        assertPageIsOpen();
        return this;
    }
    
    public ProjectProfileConfigPage save() {
        waitForClick(saveButton);
        return this;
    }
    
    public ProjectProfileConfigPage addItem() {
        waitForClick(addItemButton);
        return this;
    }
    
    public ProjectProfileConfigPage setRoleName(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='roleName']"), value);
        return this;
    }
    
    public ProjectProfileConfigPage setPeopleCount(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='peopleCount']"), value);
        return this;
    }
    
    public ProjectProfileConfigPage setAllocationStart(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='allocationStart']"), value);
        return this;
    }
    
    public ProjectProfileConfigPage setAllocationEnd(int rowIndex, String value) {
        setInputValue(selectElementInsideRow(rowIndex, "input[name='allocationEnd']"), value);
        return this;
    }

    private WebElement selectElementInsideRow(int rowIndex, String elementSelector) {
        return itemsTable.findElement(By.cssSelector("tbody tr:nth-of-type(" + (rowIndex + 1) + ") " + elementSelector));
    }
    
    public ProjectProfileConfigPage assertSnackbarSavedIsOpen() {
        waitTextInElement(snackabarTitle, "Project profile saved");
        return this;
    }

    public ProjectProfileConfigPage assertItems(String... expectedRows) {
        waitAssertEquals(join(expectedRows, "\n"), () -> {
            return itemsTable.findElements(By.cssSelector("tbody tr")).stream()
                    .map(row -> row.findElements(By.tagName("input")).stream().map(e -> e.getAttribute("value")).collect(joining(" | ")))
                    .collect(joining("\n"));
        });

        return this;
    }

    public ProjectProfileConfigPage remove(int i) {
        WebElement removeButton = selectElementInsideRow(i, ".remove-button");
        waitForClick(removeButton);
        return this;
    }
}
