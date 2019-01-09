package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class TextComponent extends AbstractComponent {

    public TextComponent(WebDriver driver, By componentSelector) {
        super(driver, componentSelector);
    }

    public void assertText(String value) {
        waitTextInElement(component(), value);
    }
}
