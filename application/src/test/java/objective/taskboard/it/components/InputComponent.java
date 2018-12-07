package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InputComponent extends AbstractComponent {

    public InputComponent(WebDriver driver, By componentSelector) {
        super(driver, componentSelector);
    }

    public InputComponent assertValue(String expected) {
        waitAttributeValueInElement(component(), "value", expected);
        return this;
    }

    public void setValue(String value) {
        setInputValue(component(), value);
    }
}
