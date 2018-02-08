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

import static org.openqa.selenium.support.ui.ExpectedConditions.attributeContains;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElements;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.ObjectUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractUiFragment {
    protected WebDriver webDriver;

    public AbstractUiFragment(WebDriver driver) {
        this.webDriver = driver;
    }

    public void waitUntil(ExpectedCondition<?> condition) {
        PageWait.wait(webDriver).until((Function<? super WebDriver, ?>) condition);
    }

    protected void waitTextInElement(WebElement element, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(textToBePresentInElement(element, expected));
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

    protected void waitForClick(WebElement element) {
        waitVisibilityOfElement(element);
        waitUntil(elementToBeClickable(element));
        element.click();
    }

    protected void waitPaperDropdownMenuSelectedValueToBe(WebElement element, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(ExpectedConditions.or(
                textToBePresentInElement(element, expected)
                , attributeToBe(element.findElement(By.cssSelector("#labelAndInputContainer input")), "value", expected)
        ));
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

    protected WebElement getPaperDropdownMenuItemByValue(WebElement dropDownMenu, String itemValue) {
        WebElement menuItem = dropDownMenu.findElements(By.tagName("paper-item")).stream()
                .filter(paperItem -> ObjectUtils.equals(itemValue, paperItem.getAttribute("value")))
                .findFirst().orElse(null);

        if (menuItem == null)
            throw new IllegalArgumentException("Element \"" + itemValue + "\" not found");

        return menuItem;
    }

    protected void waitPaperDropdownMenuItemIsSelected(WebElement dropDownMenuItem, Boolean selected) {
        waitUntil(attributeToBe(dropDownMenuItem, "aria-selected", String.valueOf(selected)));
    }

    protected void waitUntilPaperCheckboxSelectionStateToBe(WebElement element, Boolean selected) {
        waitUntil(attributeToBe(element, "aria-checked", String.valueOf(selected)));
    }

}