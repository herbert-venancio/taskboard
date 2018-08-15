package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import objective.taskboard.it.AbstractUiFragment;

abstract class AbstractComponent extends AbstractUiFragment {

    protected By componentSelector;

    protected AbstractComponent(WebDriver driver, By componentSelector) {
        super(driver);
        this.componentSelector = componentSelector;
    }

    protected WebElement component() {
        return getElementWhenItExists(componentSelector);
    }

}
