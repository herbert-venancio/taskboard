package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckboxComponent extends AbstractComponent {

    public CheckboxComponent(WebDriver driver, By by) {
        super(driver, by);
    }

    public void select() {
        isVisible();
        if (!component().isSelected())
            waitForClick(component());

    }

    public void unselect() {
        isVisible();
        if (component().isSelected())
            waitForClick(component());
    }
}
