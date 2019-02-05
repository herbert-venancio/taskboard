package objective.taskboard.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TagPicker extends AbstractUiFragment {

    private final WebElement parent;
    private final String cssSelector;

    private WebElement tagPicker;
    private WebElement input;

    private TagPicker(WebDriver driver, WebElement parent, String cssSelector) {
        super(driver);
        this.parent = parent;
        this.cssSelector = cssSelector;
    }

    public static TagPicker init(WebDriver webDriver, WebElement parent, String cssSelector) {
        return new TagPicker(webDriver, parent, cssSelector).init();
    }

    private TagPicker init() {
        tagPicker = getChildElementWhenExists(parent, By.cssSelector(cssSelector));
        waitVisibilityOfElement(tagPicker);
        input = getChildElementWhenExists(tagPicker, By.cssSelector("input[slot='input']"));
        waitVisibilityOfElement(input);
        return this;
    }

    public TagPicker select(String value) {
        waitForClick(input);
        input.sendKeys(value);

        waitUntilElementExistsWithText(By.cssSelector(cssSelector + " #suggestionsWrapper paper-item.active .paper-autocomplete-suggestions"), value);
        waitForClick(By.cssSelector(cssSelector +" #suggestionsWrapper paper-item.active"));
        return this;
    }

}
