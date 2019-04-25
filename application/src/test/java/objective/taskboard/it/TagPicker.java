package objective.taskboard.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

public class TagPicker extends AbstractUiFragment {

    private final By inputSelector;
    private final By autocompleteSuggestionSelector;
    private final By suggestionSelector;

    public TagPicker(WebDriver driver, By selector) {
        super(driver);
        this.inputSelector = new ByChained(selector, By.cssSelector("input[slot='input']"));
        this.autocompleteSuggestionSelector = new ByChained(selector, By.cssSelector("#suggestionsWrapper paper-item.active .paper-autocomplete-suggestions"));
        this.suggestionSelector = new ByChained(selector, By.cssSelector("#suggestionsWrapper paper-item.active"));
    }

    public TagPicker select(String value) {
        waitForClick(inputSelector);
        input().sendKeys(value);

        waitUntilElementExistsWithText(autocompleteSuggestionSelector, value);
        waitForClick(suggestionSelector);
        return this;
    }

    private WebElement input() {
        return getElementWhenItExistsAndIsVisible(inputSelector);
    }
}
