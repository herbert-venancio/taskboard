package objective.taskboard.it;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import static org.openqa.selenium.support.PageFactory.initElements;

import java.text.MessageFormat;

import org.openqa.selenium.By;

public class ProjectConfigurationDialog extends AbstractUiFragment {

    private static final String SUCCESS_MESSAGE = "Configuration for project {0} has been updated.";

    private WebElement projectItemButton;

    @FindBy(id="projectConfigurationModal")
    private WebElement dialog;

    private WebElement startDateInput;

    private WebElement deliveryDateInput;

    private WebElement projectionTimespanInput;

    private WebElement updateButton;

    public ProjectConfigurationDialog(WebDriver driver) {
        super(driver);
    }

    public static ProjectConfigurationDialog open(WebDriver webDriver, WebElement projectItemButton) {
        return initElements(webDriver, ProjectConfigurationDialog.class).open(projectItemButton);
    }

    private ProjectConfigurationDialog open(WebElement projectItemButton) {
        this.projectItemButton = projectItemButton;
        waitForClick(this.projectItemButton);
        waitVisibilityOfElement(dialog);
        
        startDateInput = dialog.findElement(By.id("projectStartDate"));
        deliveryDateInput = dialog.findElement(By.id("projectDeliveryDate"));
        projectionTimespanInput = dialog.findElement(By.id("projectProjectionTimespan"));
        updateButton = dialog.findElement(By.id("updateProjectConfiguration"));
        waitVisibilityOfElements(startDateInput, deliveryDateInput, projectionTimespanInput, updateButton);

        return this;
    }

    public ProjectConfigurationDialog close() {
        WebElement close = dialog.findElement(By.cssSelector(".button-close"));
        waitForClick(close);
        return this;
    }

    public ProjectConfigurationDialog updateConfiguration(String startDate, String deliveryDate, Integer projection) {
        // clean-up dialog
        open(projectItemButton);

        startDateInput.sendKeys(startDate);
        deliveryDateInput.sendKeys(deliveryDate);
        projectionTimespanInput.sendKeys(projection.toString());
        waitForClick(updateButton);
        assertSuccessMessage(MessageFormat.format(SUCCESS_MESSAGE, projectItemButton.getText()));
        closeAlertDialog();
        return this;
    }

    public ProjectConfigurationDialog tryUpdateWithInvalidProjectionTimespan() {
        // clean-up dialog
        open(projectItemButton);

        startDateInput.sendKeys("01/01/2018");
        deliveryDateInput.sendKeys("02/01/2018");
        projectionTimespanInput.sendKeys(Integer.valueOf(-1).toString());
        waitForClick(updateButton);
        assertErrorMessage("Projection timespan should be a positive number");
        close();
        return this;
    }

    public ProjectConfigurationDialog tryUpdateWithInvalidDateRange() {
        // clean-up dialog
        open(projectItemButton);

        startDateInput.sendKeys("04/01/2018");
        deliveryDateInput.sendKeys("02/01/2018");
        projectionTimespanInput.sendKeys(Integer.valueOf(1).toString());
        waitForClick(updateButton);
        assertErrorMessage("End Date should be greater than Start Date");
        close();
        return this;
    }

    private void assertSuccessMessage(String message) {
        WebElement successMessage = dialog.findElement(By.cssSelector("#alertModalConfiguration .text"));
        waitTextInElement(successMessage, message);
    }

    private void assertErrorMessage(String message) {
        WebElement errorMessage = dialog.findElement(By.cssSelector(".error-message"));
        waitTextInElement(errorMessage, message);
    }

    private void closeAlertDialog() {
        WebElement closeAlertDialog = dialog.findElement(By.cssSelector("#alertModalConfiguration #ok"));
        waitForClick(closeAlertDialog);
    }

}
