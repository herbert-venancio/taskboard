package objective.taskboard.it;

import static org.openqa.selenium.support.PageFactory.initElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FieldsRequiredModal extends AbstractUiFragment {

    private WebElement confirmButton;

    @FindBy(css = "#fieldsRequiredDialog .modal")
    private WebElement fieldsRequiredModal;

    public FieldsRequiredModal(WebDriver driver) {
        super(driver);
    }

    public static FieldsRequiredModal open(WebDriver webDriver) {
        return initElements(webDriver, FieldsRequiredModal.class).open();
    }

    private FieldsRequiredModal open() {
        waitVisibilityOfElement(fieldsRequiredModal);
        confirmButton = getChildElementWhenExists(fieldsRequiredModal, By.id("confirm-button"));
        return this;
    }

    public FieldsRequiredModal confirm() {
        waitForClick(confirmButton);
        return this;
    }

    public FieldsRequiredModal assertRequiredMessageIsVisible(String fieldId) {
        WebElement error = getChildElementWhenExists(fieldsRequiredModal, By.id("error-" + fieldId));
        waitVisibilityOfElement(error);
        return this;
    }

    public FieldsRequiredModal addVersion(String fieldId, String value) {
        WebElement versionField = getChildElementWhenExists(fieldsRequiredModal, By.cssSelector("version-field#" + fieldId));
        WebElement addVersion = getChildElementWhenExists(versionField, By.id("addVersionButton"));
        waitForClick(addVersion);

        TagPicker versionPicker = TagPicker.init(webDriver, versionField, "#pickerForAddVersion");
        versionPicker.select(value);
        return this;
    }

    public FieldsRequiredModal assertIsClosed() {
        waitInvisibilityOfElement(fieldsRequiredModal);
        return this;
    }

}
