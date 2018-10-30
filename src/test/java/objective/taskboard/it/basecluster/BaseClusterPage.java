package objective.taskboard.it.basecluster;

import static objective.taskboard.it.AbstractIntegrationTest.getAppBaseUrl;
import static objective.taskboard.it.components.SnackBarComponent.SNACK_BAR_TAG;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.AbstractAppUiFragment;
import objective.taskboard.it.components.SnackBarComponent;
import objective.taskboard.it.components.guards.LeaveConfirmationGuard;

public class BaseClusterPage extends AbstractAppUiFragment {

    private static final String PAGE_TAG = "tb-base-cluster";

    @FindBy(css=PAGE_TAG + " #tb-page-title")
    private WebElement pageTitle;

    @FindBy(css=PAGE_TAG + " input[name=baseClusterName]")
    private WebElement name;

    @FindBy(css=PAGE_TAG + " button#tb-project-cluster-save")
    private WebElement saveButton;

    @FindBy(css=PAGE_TAG + " button#tb-project-cluster-back-to-project")
    private WebElement backToSearchButton;

    private SnackBarComponent snackbar;

    public BaseClusterPage(WebDriver webDriver) {
        super(webDriver);
        initElements(webDriver, this);
        assertPageIsOpen();

        this.snackbar = new SnackBarComponent(webDriver, cssSelector(SNACK_BAR_TAG + "#tb-base-cluster-snackbar"));
    }

    public static String getCreatePageUrl() {
        return getAppBaseUrl() + "base-cluster/new";
    }

    public static String getEditionPageUrl(final int clusterId) {
        return getAppBaseUrl() + "base-cluster/" + clusterId;
    }

    public BaseClusterPage assertName(final String expectedName) {
        waitAttributeValueInElement(name, "value", expectedName);
        return this;
    }

    public BaseClusterPage setName(final String newName) {
        setInputValue(name, newName);
        return this;
    }

    public BaseClusterPage setEffort(final String issueType, final String size, final String effort) {
        WebElement input = getElementWhenItExistsAndIsVisible(By.id(issueType + "-effort-" + size));
        setInputValue(input, effort);
        return this;
    }

    public BaseClusterPage setCycle(final String issueType, final String size, final String cycle) {
        WebElement input = getElementWhenItExistsAndIsVisible(By.id(issueType + "-cycle-" + size));
        setInputValue(input, cycle);
        return this;
    }

    public BaseClusterPage assertSaveButtonEnabled() {
        waitElementIsEnabled(saveButton);
        return this;
    }

    public BaseClusterPage assertSaveButtonDisabled() {
        waitElementIsDisabled(saveButton);
        return this;
    }

    public BaseClusterPage save() {
        waitForClick(saveButton);
        return this;
    }

    public BaseClusterPage assertErrorMessageIsOpen() {
        snackbar.waitTitleToBe("Error");
        snackbar.waitDescriptionToBe("Please review the form");
        return this;
    }

    public BaseClusterPage assertSuccessMessageIsOpen() {
        snackbar.waitTitleToBe("Success");
        snackbar.waitDescriptionToBe("Base cluster saved");
        return this;
    }

    public BaseClusterSearchPage backToSearchPage() {
        waitForClick(backToSearchButton);
        return new BaseClusterSearchPage(webDriver);
    }

    public BaseClusterPage assertCycle(final String issueType, final String size, final String expectedCycle) {
        WebElement input = getElementWhenItExistsAndIsVisible(By.id(issueType + "-cycle-" + size));
        waitAttributeValueInElement(input, "value", expectedCycle);
        return this;
    }

    public BaseClusterPage assertEffort(final String issueType, final String size, final String expectedEffort) {
        WebElement input = getElementWhenItExistsAndIsVisible(By.id(issueType + "-effort-" + size));
        waitAttributeValueInElement(input, "value", expectedEffort);
        return this;
    }

    public BaseClusterPage refreshPage() {
        webDriver.navigate().refresh();
        assertPageIsOpen();
        return this;
    }

    public BaseClusterSearchPage backToSearchPageConfirmingLoseChanges() {
        waitForClick(backToSearchButton);
        LeaveConfirmationGuard.leave(webDriver);
        return new BaseClusterSearchPage(webDriver);
    }

    private BaseClusterPage assertPageIsOpen() {
        waitTextInElement(pageTitle, "Base Cluster");
        waitTextInElement(pageTitle, ">");
        waitPageLoaderBeHide();
        return this;
    }
}
