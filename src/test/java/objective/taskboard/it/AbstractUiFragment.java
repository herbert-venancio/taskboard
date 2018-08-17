/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

package objective.taskboard.it;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeContains;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElements;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class AbstractUiFragment {
    protected WebDriver webDriver;

    public AbstractUiFragment(WebDriver driver) {
        this.webDriver = driver;
    }

    public void waitUntil(ExpectedCondition<?> condition) {
        PageWait.wait(webDriver).until((Function<? super WebDriver, ?>) condition);
    }
    
    public <T> void waitAssertEquals(T expected, Supplier<T> actualSupplier) {
        try {
            waitUntil(w -> {
                return expected.equals(actualSupplier.get());
            });
        } catch (TimeoutException e) {
            assertEquals(expected, actualSupplier.get());
        }
    }

    protected void scroolToElement(WebElement element) {
        executeJavascript("arguments[0].scrollIntoView(true);", element);
    }

    protected void waitTextInElement(WebElement element, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(textToBePresentInElement(element, expected));
    }
    
    protected void waitUntilElementExistsWithText(By by, String valueToSelect) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver element) {
                try {
                	List<WebElement> elements = element.findElements(by);
                	if (elements.size() == 0)
                		return false;
                	WebElement we = elements.get(0);
                	if (!we.isDisplayed())
                		return false;
                    return we.getText().trim().equals(valueToSelect);
                }catch(StaleElementReferenceException ex) {
                    return false;
                }
            }
        });
    }

    protected void waitAttributeValueInElement(WebElement element, String attribute, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(attributeToBe(element, attribute, expected));
    }

    protected void waitAttributeValueInElementIsNot(WebElement element, String attribute, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(not(attributeToBe(element, attribute, expected)));
    }

    protected void waitAttributeValueInElementContains(WebElement element, String attribute, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(attributeContains(element, attribute, expected));
    }

    protected void waitAttributeValueInElementNotContains(WebElement element, String attribute, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(not(attributeContains(element, attribute, expected)));
    }

    protected void waitVisibilityOfElement(WebElement element) {
        waitUntil(visibilityOf(element));
    }

    protected void ensureVisibilityOfElementDuringMilliseconds(WebElement element, Long milliseconds) {
        Long end = System.currentTimeMillis() + milliseconds;
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
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver element) {
                return element.findElements(by).size() > 0;
            }
        });
    }
    
    protected void waitUntilElementsShowsUpCountTimes(By by, int count) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver element) {
                return element.findElements(by).size() == count;
            }
        });
    }

    protected void waitUntilElementNotExists(By by) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return input.findElements(by).size() == 0;
            }
        });
    }

    protected WebElement getElementWhenItExists(By by) {
        waitUntilElementExists(by);
        return webDriver.findElement(by);
    }

    protected List<WebElement> getElementsWhenTheyExists(By by) {
        waitUntilElementExists(by);
        return webDriver.findElements(by);
    }

    protected void waitUntilChildElementExists(WebElement element, By by) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return childElementExists(element, by);
            }
        });
    }

    protected void waitUntilChildElementNotExists(WebElement element, By by) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return childElementExists(element, by) == false;
            }
        });
    }

    protected boolean childElementExists(WebElement element, By by) {
        return element.findElements(by).size() > 0;
    }

    protected WebElement getChildElementWhenExists(WebElement element, By by) {
        waitUntilChildElementExists(element, by);
        return element.findElement(by);
    }

    protected List<WebElement> getChildrenElementsWhenTheyExists(WebElement element, By by) {
        waitUntilChildElementExists(element, by);
        return element.findElements(by);
    }

    protected void waitForClick(WebElement element) {
        waitVisibilityOfElement(element);
        waitUntil(elementToBeClickable(element));
        element.click();
    }
    
    protected void waitForClickHoldingAKey(WebElement element, Keys keyHolding) {
        waitVisibilityOfElement(element);
        waitUntil(elementToBeClickable(element));
        
        new Actions(webDriver)
            .keyDown(keyHolding)
            .click(element)
            .perform();
    }
    
    protected void waitForClick(By by) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver element) {
                List<WebElement> elements = element.findElements(by);
                if (elements.size() == 0)
                    return false;
                try {
                    WebElement we = elements.get(0);
                    if (!we.isDisplayed())
                        return false;
                    we.click();
                    return true;
                }catch(StaleElementReferenceException ex) {
                    return false;
                }
            }
        });
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
                            .filter(paperItem -> expected.equals(paperItem.getAttribute("textContent").trim()))
                            .findFirst().isPresent();
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

    protected void waitUntilPaperCheckboxSelectionStateToBe(WebElement element, Boolean selected) {
        waitUntil(attributeToBe(element, "aria-checked", String.valueOf(selected)));
    }

    protected void selectPaperDropdownItem(WebElement dropdown, String itemText) {
        waitForClick(dropdown);
        WebElement dateElement = getPaperDropdownMenuItemByText(dropdown, itemText);
        waitForClick(dateElement);
        waitInvisibilityOfElement(dateElement);
        waitPaperDropdownMenuSelectedTextToBe(dropdown, itemText);
    }

    protected void waitElementIsDisabled(WebElement element) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return element.getAttribute("disabled") != null;
            }
        });
    }

    protected void waitElementIsEnabled(WebElement element) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return element.getAttribute("disabled") == null;
            }
        });
    }

    protected void toggleElementVisibility(By elementToToggle, Runnable toggle) {
        boolean isVisibleAndExists = isElementVisibleAndExists(elementToToggle);
        toggle.run();
        waitElementExistenceAndVisibilityIs(!isVisibleAndExists, elementToToggle);
    }

    protected void waitElementExistenceAndVisibilityIs(boolean isVisibleAndExists, By selector) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return isElementVisibleAndExists(selector) == isVisibleAndExists;
            }
        });
    }

    protected boolean isElementVisibleAndExists(By selector) {
        List<WebElement> elements = webDriver.findElements(selector);
        return elements.size() > 0 && elements.get(0).isDisplayed();
    }

    protected void waitElementNotExistsOrInvisible(By selector) {
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                List<WebElement> elements = webDriver.findElements(selector);
                return elements.size() == 0 || !elements.get(0).isDisplayed();
            }
        });
    }

    protected boolean hasClass(WebElement element, String className) {
        return Stream.of(element.getAttribute("class").split(" ")).anyMatch(classItem -> classItem.equals(className));
    }

    private void executeJavascript(String script, Object... args) {
        if (!(webDriver instanceof RemoteWebDriver)) 
            throw new RuntimeException("WebDriver " + webDriver + " is unable to execute javascript");
        
        RemoteWebDriver remoteWebDriver = (RemoteWebDriver) webDriver;
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