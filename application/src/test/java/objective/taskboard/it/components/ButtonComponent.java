package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ButtonComponent extends AbstractComponent {

    public static final String BUTTON_TAG = "button";

    public ButtonComponent(WebDriver driver, By by) {
        super(driver, by);
    }

    public void click() {
        waitForClick(component());
    }

    public boolean isDisabled() {
        return component().getAttribute("disabled") != null;
    }

    public void waitDisabledBe(boolean disabled) {
        waitAssertEquals(disabled, () -> isDisabled());
    }

}
