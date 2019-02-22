package objective.taskboard.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AlertModal extends AbstractUiFragment {

    private WebElement alertModal;
    private WebElement text;
    private WebElement okButton;

    private AlertModal(WebDriver driver) {
        super(driver);
    }

    public static AlertModal init(WebDriver driver, WebElement parent) {
        return new AlertModal(driver).init(parent);
    }

    private AlertModal init(WebElement parent) {
        alertModal = getChildElementWhenExists(parent, By.cssSelector("#alertModal"));
        waitVisibilityOfElement(alertModal);
        text = getChildElementWhenExists(alertModal, By.className("text"));
        okButton = getChildElementWhenExists(alertModal, By.id("ok"));
        waitVisibilityOfElement(okButton);
        return this;
    }

    public AlertModal assertMessage(String message) {
        waitTextInElement(text, message);
        return this;
    }

    public AlertModal clickOnOk() {
        waitForClick(okButton);
        return this;
    }

}
