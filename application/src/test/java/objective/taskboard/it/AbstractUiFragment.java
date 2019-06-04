package objective.taskboard.it;

import static objective.taskboard.it.PageWait.atLeastOneElementExists;
import static objective.taskboard.it.PageWait.attributeContains;
import static objective.taskboard.it.PageWait.attributeToBe;
import static objective.taskboard.it.PageWait.elementClicked;
import static objective.taskboard.it.PageWait.elementExist;
import static objective.taskboard.it.PageWait.elementIsClickable;
import static objective.taskboard.it.PageWait.elementIsVisible;
import static objective.taskboard.it.PageWait.elementTextContains;
import static objective.taskboard.it.PageWait.elementTextIs;
import static objective.taskboard.it.PageWait.noneExists;
import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElements;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import objective.taskboard.it.PageWait.WebElementCondition;
import objective.taskboard.it.PageWait.WebElementsCondition;

public abstract class AbstractUiFragment {
    protected WebDriver webDriver;

    public AbstractUiFragment(WebDriver driver) {
        this.webDriver = driver;
    }

    public static void waitUntil(WebDriver driver, ExpectedCondition<?> condition) {
        PageWait.wait(driver).until(condition);
    }

    public void waitUntil(ExpectedCondition<?> condition) {
        waitUntil(webDriver, condition);
    }

    protected WebElement waitAllConditions(By selector, WebElementCondition... conditions) {
        return new PageWait(webDriver).allConditions(selector, conditions);
    }

    protected List<WebElement> waitAllConditions(By selector, WebElementsCondition... conditions) {
        return new PageWait(webDriver).allConditions(selector, conditions);
    }

    protected WebElement waitAllConditions(WebElement element, WebElementCondition... conditions) {
        return new PageWait(webDriver).allConditions(element, conditions);
    }

    protected WebElement waitAllConditions(WebElement parent, By selector, WebElementCondition... conditions) {
        return new PageWait(webDriver).allConditions(parent, selector, conditions);
    }

    protected List<WebElement> waitAllConditions(WebElement parent, By selector, WebElementsCondition... conditions) {
        return new PageWait(webDriver).allConditions(parent, selector, conditions);
    }

    public <T> void waitAssertEquals(T expected, Supplier<T> actualSupplier) {
        try {
            waitUntil(w -> expected.equals(actualSupplier.get()));
        } catch (TimeoutException e) {
            assertEquals(expected, actualSupplier.get());
        }
    }

    public void waitTrue(Supplier<Boolean> actualSupplier) {
        waitAssertEquals(true, actualSupplier);
    }

    protected void scrollToElement(WebElement element) {
        executeJavascript("arguments[0].scrollIntoView(true);", element);
    }

    protected void waitTextInElement(WebElement element, String expected) {
        waitAllConditions(element, elementIsVisible(), elementTextContains(expected));
    }

    protected void waitUntilElementExistsWithText(By by, String valueToSelect) {
        waitAllConditions(by, elementIsVisible(), elementTextIs(valueToSelect));
    }

    protected void waitAttributeValue(By by, String attribute, String expected) {
        waitAllConditions(by, elementIsVisible(), attributeToBe(attribute, expected));
    }

    protected void waitAttributeValueInElement(WebElement element, String attribute, String expected) {
        waitAllConditions(element, elementIsVisible(), attributeToBe(attribute, expected));
    }

    protected void waitAttributeValueInElementIsNot(WebElement element, String attribute, String expected) {
        waitAllConditions(element, elementIsVisible(), attributeToBe(attribute, expected).negate());
    }

    protected void waitAttributeValueInElementContains(WebElement element, String attribute, String expected) {
        waitAllConditions(element, elementIsVisible(), attributeContains(attribute, expected));
    }

    protected void waitVisibilityOfElement(WebElement element) {
        waitAllConditions(element, elementIsVisible());
    }

    protected void waitVisibilityOfElement(By by) {
        waitAllConditions(by, elementIsVisible());
    }

    protected void ensureVisibilityOfElementDuringMilliseconds(WebElement element, Long milliseconds) {
        long end = System.currentTimeMillis() + milliseconds;
        while (System.currentTimeMillis() < end)
            waitUntil(visibilityOf(element));
    }

    protected void waitVisibilityOfElements(WebElement... elements) {
        for (int i = 0; i < elements.length; ++i) {
            waitVisibilityOfElement(elements[i]);
        }
    }

    protected void waitVisibilityOfElementList(List<WebElement> elementList) {
        waitUntil(visibilityOfAllElements(elementList));
    }

    protected void waitInvisibilityOfElement(WebElement element) {
        waitUntil(invisibilityOf(element));
    }

    protected void waitUntilElementExists(By by) {
        waitAllConditions(by, atLeastOneElementExists());
    }

    protected void waitUntilElementNotExists(By by) {
        waitAllConditions(by, noneExists());
    }

    protected WebElement getElementWhenItExists(By by) {
        return waitAllConditions(by, elementExist());
    }

    protected WebElement getElementWhenItExistsAndIsVisible(By by) {
        return waitAllConditions(by, elementExist(), elementIsVisible());
    }

    protected List<WebElement> getElementsWhenTheyExists(By by) {
        return waitAllConditions(by, atLeastOneElementExists());
    }

    protected WebElement waitUntilChildElementExists(WebElement element, By by) {
        return waitAllConditions(element, by, elementIsVisible());
    }

    protected void waitUntilChildElementNotExists(WebElement element, By by) {
        waitAllConditions(element, by, noneExists());
    }

    protected boolean childElementExists(WebElement element, By by) {
        return element.findElements(by).size() > 0;
    }

    protected WebElement getChildElementWhenExists(WebElement element, By by) {
        return waitAllConditions(element, by, elementExist());
    }

    protected List<WebElement> getChildrenElementsWhenTheyExists(WebElement element, By by) {
        return waitAllConditions(element, by, atLeastOneElementExists());
    }

    protected void waitForClick(WebElement element) {
        waitAllConditions(element, elementIsVisible(), elementIsClickable(), elementClicked());
    }

    protected void waitForClickHoldingAKey(WebElement element, Keys keyHolding) {
        waitVisibilityOfElement(element);
        waitUntil(elementToBeClickable(element));

        new Actions(webDriver)
            .moveToElement(element)
            .keyDown(keyHolding)
            .click()
            .perform();
    }

    protected void waitForClick(By by) {
        waitAllConditions(by, elementIsVisible(), elementIsClickable(), elementClicked());
    }

    protected void waitForHover(By by) {
        new Actions(webDriver).moveToElement(getElementWhenItExistsAndIsVisible(by)).perform();
    }

    protected void setInputValue(WebElement input, String value) {
        input.clear();
        input.sendKeys(value);
        waitAttributeValueInElement(input, "value", value);
        executeJavascript("arguments[0].blur()", input);
    }

    protected void waitPaperDropdownMenuSelectedTextToBe(WebElement element, String expected) {
        waitVisibilityOfElement(element);

        waitUntil(new ExpectedCondition<Boolean>() {
            private String actual;

            @Override
            public Boolean apply(WebDriver driver) {
                actual = element.findElements(By.cssSelector("paper-item[aria-selected='true']")).stream()
                        .map(i -> i.getAttribute("textContent").trim())
                        .findFirst()
                        .orElse("<not-selected>");

                return actual.equals(expected);
            }

            @Override
            public String toString() {
                return String.format("selected item text ('%s'), actual ('%s') ", expected, actual);
            }
        });
    }

    protected void waitPaperDropdownMenuContains(WebElement dropDownMenu, String expected) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return dropDownMenu.findElements(By.tagName("paper-item")).stream()
                            .anyMatch(paperItem -> expected.equals(paperItem.getAttribute("textContent").trim()));
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return String.format("Release filter contains ('%s')", expected);
            }
        });
    }

    protected WebElement getPaperDropdownMenuItemByText(WebElement dropDownMenu, String itemText) {
        WebElement menuItem = dropDownMenu.findElements(By.tagName("paper-item")).stream()
                .filter(paperItem -> Objects.equals(itemText, paperItem.getText().trim()))
                .findFirst().orElse(null);

        if (menuItem == null)
            throw new IllegalArgumentException("Dropdown item with text \"" + itemText + "\" not found");

        return menuItem;
    }

    protected void selectPaperDropdownItem(WebElement dropdown, String itemText) {
        waitForClick(dropdown);
        WebElement dateElement = getPaperDropdownMenuItemByText(dropdown, itemText);
        waitForClick(dateElement);
        waitInvisibilityOfElement(dateElement);
        waitPaperDropdownMenuSelectedTextToBe(dropdown, itemText);
    }

    protected void waitElementIsDisabled(WebElement element) {
        waitUntil((ExpectedCondition<Boolean>) input -> element.getAttribute("disabled") != null);
    }

    protected void waitElementIsEnabled(WebElement element) {
        waitUntil((ExpectedCondition<Boolean>) input -> element.getAttribute("disabled") == null);
    }

    protected void toggleElementVisibility(By elementToToggle, Runnable toggle) {
        boolean isVisibleAndExists = isElementVisibleAndExists(elementToToggle);
        toggle.run();
        waitElementExistenceAndVisibilityIs(!isVisibleAndExists, elementToToggle);
    }

    protected void waitElementExistenceAndVisibilityIs(boolean isVisibleAndExists, By selector) {
        waitUntil((ExpectedCondition<Boolean>) input -> isElementVisibleAndExists(selector) == isVisibleAndExists);
    }

    protected boolean isElementVisibleAndExists(By selector) {
        try {
            WebElement element = webDriver.findElement(selector);
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected void waitElementNotExistsOrInvisible(By selector) {
        waitUntil((ExpectedCondition<Boolean>) input -> !isElementVisibleAndExists(selector));
    }

    protected boolean hasClass(WebElement element, String className) {
        return Stream.of(element.getAttribute("class").split(" ")).anyMatch(classItem -> classItem.equals(className));
    }

    private void executeJavascript(String script, Object... args) {
        if (!(webDriver instanceof JavascriptExecutor))
            throw new RuntimeException("WebDriver " + webDriver + " is unable to execute javascript");

        JavascriptExecutor remoteWebDriver = (JavascriptExecutor) webDriver;
        remoteWebDriver.executeScript(script, args);
    }

    /**
     * Geckodriver implements a webdriver spec that requires inputs to be visible during interactions.
     * On the other hand, it's common to set file inputs hidden and use a placeholder for better looking.
     * This method turns the field temporary visible to allow the "sendKeys" command execution.<br>
     *
     * https://github.com/w3c/webdriver/issues/1230
     */
    protected void sendKeysToFileInput(WebElement fileInput, String value) {
        if (!(webDriver instanceof FirefoxDriver)) {
            fileInput.sendKeys(value);
            return;
        }

        executeJavascript("arguments[0].style.display='block'", fileInput);
        waitVisibilityOfElement(fileInput);
        fileInput.sendKeys(value);
        executeJavascript("arguments[0].style.display=''", fileInput);
    }

}