package objective.taskboard.it;

import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

public class FieldsRequiredModal extends AbstractUiFragment {

    private WebElement confirmButton;

    private static final By fieldsRequiredModalSelector = By.cssSelector("#fieldsRequiredDialog .modal");

    public FieldsRequiredModal(WebDriver driver) {
        super(driver);
    }

    public static FieldsRequiredModal open(WebDriver webDriver) {
        return initElements(webDriver, FieldsRequiredModal.class).open();
    }

    private FieldsRequiredModal open() {
        waitVisibilityOfElement(fieldsRequiredModal());
        By buttonSelector = new ByChained(fieldsRequiredModalSelector, By.id("confirm-button"));
        confirmButton = getElementWhenItExists(buttonSelector);
        return this;
    }

    public FieldsRequiredModal confirm() {
        waitForClick(confirmButton);
        return this;
    }

    public FieldsRequiredModal assertRequiredMessageIsVisible(String fieldId) {
        By errorFieldSelector = new ByChained(fieldsRequiredModalSelector, By.id("error-" + fieldId));
        WebElement error = getElementWhenItExists(errorFieldSelector);
        waitVisibilityOfElement(error);
        return this;
    }

    public FieldsRequiredModal addVersion(String fieldId, String value) {
        By versionFieldSelector = new ByChained(fieldsRequiredModalSelector, By.cssSelector("version-field#" + fieldId));
        WebElement versionField = getElementWhenItExists(versionFieldSelector);
        WebElement addVersion = getChildElementWhenExists(versionField, By.id("addVersionButton"));
        waitForClick(addVersion);

        TagPicker versionPicker = new TagPicker(webDriver, new ByChained(versionFieldSelector, By.cssSelector("#pickerForAddVersion")));
        versionPicker.select(value);
        return this;
    }

    public FieldsRequiredModal assertIsClosed() {
        waitInvisibilityOfElement(webDriver.findElement(fieldsRequiredModalSelector));
        return this;
    }

    private WebElement fieldsRequiredModal() {
        return getElementWhenItExistsAndIsVisible(fieldsRequiredModalSelector);
    }

}
