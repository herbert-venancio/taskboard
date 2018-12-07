package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ErrorMessagesComponent extends AbstractComponent {

    public ErrorMessagesComponent(WebDriver driver, By by) {
        super(driver, by);
    }

    public boolean hasErrorMessages() {
        return hasClass(component(), "has-messages");
    }

}
