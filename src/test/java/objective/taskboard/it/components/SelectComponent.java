package objective.taskboard.it.components;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SelectComponent extends AbstractComponent {

    public static final String SELECT_TAG = "ng-select";

    private final By selectedOptionLabel = cssSelector(".ng-value-label");

    public SelectComponent(WebDriver driver, By by) {
        super(driver, by);
    }

    public void select(String optionName) {
        select(optionName, false);
    }

    public void select(String optionName, boolean elementRemovedAfterSelection) {
        waitForClick(selectContainerEl());
        waitDropdownIsOpened();
        autocompleteValue(optionName);
        waitForClick(optionEl(optionName));
        if (!elementRemovedAfterSelection)
            waitValueIsSelected(optionName);
    }

    private void autocompleteValue(String text) {
        setInputValue(inputEl(), text);
    }

    public String getValue() {
        return !hasValueSelected() ? "" : selectedValueEl().getText();
    }

    public boolean isDisabled() {
        return hasClass(component(), "ng-select-disabled");
    }

    public void waitValueIsSelected(String optionName) {
        if (isEmpty(optionName))
            waitUntilChildElementNotExists(selectContainerEl(), selectedOptionLabel);
        else
            waitTextInElement(selectedValueEl(), optionName);
    }

    private void waitDropdownIsOpened() {
        waitVisibilityOfElement(dropdownEl());
    }

    private boolean hasValueSelected() {
        return childElementExists(selectContainerEl(), selectedOptionLabel);
    }

    private WebElement selectContainerEl() {
        return getChildElementWhenExists(component(), cssSelector(".ng-select-container"));
    }


    private WebElement selectedValueEl() {
        return getChildElementWhenExists(selectContainerEl(), selectedOptionLabel);
    }

    private WebElement inputEl() {
        return getChildElementWhenExists(component(), cssSelector(".ng-input input"));
    }

    private WebElement dropdownEl() {
        return getChildElementWhenExists(component(), cssSelector("ng-dropdown-panel"));
    }

    private List<WebElement> optionsEl() {
        return getChildrenElementsWhenTheyExists(component(), cssSelector(".ng-option-label"));
    }

    private WebElement optionEl(String optionName) {
        waitTrue(() -> optionsEl().stream().anyMatch(option -> option.getText().equals(optionName)));
        return optionsEl().stream()
            .filter(option -> option.getText().equals(optionName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Option \""+ optionName + "\" not found."));
    }

}
