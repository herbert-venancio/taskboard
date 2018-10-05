package objective.taskboard.it.components;

import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SearchComponent extends AbstractComponent {

    public static final String SEARCH_TAG = "obj-search";

    private final By input = cssSelector("input.text");
    private final By clear = cssSelector(".clear");

    public SearchComponent(WebDriver driver, By componentSelector) {
        super(driver, componentSelector);
    }

    public void search(String value) {
        waitForClick(inputEl());
        setInputValue(inputEl(), value);
    }

    public void clear() {
        waitForClick(clearEl());
        waitAttributeValueInElement(inputEl(), "value", "");
    }

    private WebElement inputEl() {
        return getChildElementWhenExists(component(), input);
    }

    private WebElement clearEl() {
        return getChildElementWhenExists(component(), clear);
    }

}
