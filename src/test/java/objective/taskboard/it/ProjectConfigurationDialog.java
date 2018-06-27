package objective.taskboard.it;

import static java.text.MessageFormat.format;
import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProjectConfigurationDialog extends AbstractUiFragment {

    private static final String SUCCESS_MESSAGE = "Configuration for project {0} has been updated.";

    private final String projectKey;

    @FindBy(id="projectConfigurationModal")
    private WebElement dialog;
    
    @FindBy(css="#projectConfigurationModal .modal__title")
    private WebElement dialogTitle;
    
    @FindBy(id="project-error")
    private WebElement globalError;

    @FindBy(css="#projectStartDate input")
    private WebElement startDateInput;
    
    @FindBy(css="#projectStartDate paper-input-error")
    private WebElement startDateError;

    @FindBy(css="#projectDeliveryDate input")
    private WebElement deliveryDateInput;
    
    @FindBy(css="#projectDeliveryDate paper-input-error")
    private WebElement deliveryDateError;
    
    @FindBy(css="#projectRisk input")
    private WebElement riskInput;
    
    @FindBy(css="#projectRisk paper-input-error")
    private WebElement riskError;
    
    @FindBy(id="baselineDate")
    private WebElement baselineDateDropdown;

    @FindBy(id="updateProjectConfiguration")
    private WebElement updateButton;

    @FindBy(id="editProjectProfileConfiguration")
    private WebElement editProjectProfileButton;

    public ProjectConfigurationDialog(WebDriver driver, String projectKey) {
        super(driver);
        this.projectKey = projectKey;
        initElements(driver, this);
    }

    public ProjectConfigurationDialog assertIsOpen() {
        waitVisibilityOfElement(dialog);
        waitVisibilityOfElements(dialogTitle, startDateInput, deliveryDateInput, riskInput, updateButton);
        waitTextInElement(dialogTitle, "Project Configuration for " + projectKey);

        return this;
    }

    public ProjectConfigurationDialog close() {
        WebElement close = dialog.findElement(By.cssSelector(".modal__close"));
        waitForClick(close);
        return this;
    }
    
    public ProjectConfigurationDialog setStartDate(String value) {
        setInputValue(startDateInput, value);
        return this;
    }
    
    public ProjectConfigurationDialog assertStartDate(String expectedValue) {
        waitAttributeValueInElement(startDateInput, "value", expectedValue);
        return this;
    }

    public ProjectConfigurationDialog assertStartDateError(String expectedError) {
        waitTextInElement(startDateError, expectedError);
        return this;
    }

    public ProjectConfigurationDialog setDeliveryDate(String value) {
        setInputValue(deliveryDateInput, value);
        return this;
    }
    
    public ProjectConfigurationDialog assertDeliveryDate(String expectedValue) {
        waitAttributeValueInElement(deliveryDateInput, "value", expectedValue);
        return this;
    }
    
    public ProjectConfigurationDialog assertDeliveryDateError(String expectedError) {
        waitTextInElement(deliveryDateError, expectedError);
        return this;
    }
    
    public ProjectConfigurationDialog setRisk(String value) {
        setInputValue(riskInput, value);
        return this;
    }
    
    public ProjectConfigurationDialog assertRisk(String expectedValue) {
        waitAttributeValueInElement(riskInput, "value", expectedValue);
        return this;
    }
    
    public ProjectConfigurationDialog assertRiskError(String expectedError) {
        waitTextInElement(riskError, expectedError);
        return this;
    }

    public ProjectConfigurationDialog update() {
        waitForClick(updateButton);
        return this;
    }
    
    public ProjectConfigurationDialog editProjectProfile() {
        waitForClick(editProjectProfileButton);
        return this;
    }
   
    public ProjectConfigurationDialog assertGlobalError(String message) {
        waitTextInElement(globalError, message);
        return this;
    }

    public void closeSuccessAlert() {
        WebElement closeAlertDialog = dialog.findElement(By.cssSelector("#alertModalConfiguration #ok"));
        waitForClick(closeAlertDialog);
    }

    public ProjectConfigurationDialog assertSuccessAlertIsOpen() {
        WebElement successMessage = dialog.findElement(By.cssSelector("#alertModalConfiguration .text"));
        waitTextInElement(successMessage, format(SUCCESS_MESSAGE, projectKey));
        return this;
    }


    public ProjectConfigurationDialog assertSaveConfirmModalIsOpen() {
        WebElement title = dialog.findElement(By.cssSelector("#saveProjectConfigurationConfirmModal h2"));
        waitTextInElement(title, "Save Changes?");
        return this;
    }

    public ProjectConfigurationDialog confirmSave() {
        waitForClick(By.cssSelector("#saveProjectConfigurationConfirmModal #confirm"));
        return this;
    }

    public ProjectConfigurationDialog cancelSave() {
        waitForClick(By.cssSelector("#saveProjectConfigurationConfirmModal #cancel"));
        return this;
    }

    public ProjectConfigurationDialog setBaselineDate(String value) {
        selectPaperDropdownItem(baselineDateDropdown, value);
        return this;
    }

    public void assertBaselineDate(String expectedValue) {
        waitPaperDropdownMenuSelectedTextToBe(baselineDateDropdown, expectedValue);
    }

}
