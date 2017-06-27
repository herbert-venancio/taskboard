package objective.taskboard.it;

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

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class AbstractUiFragment {
    protected WebDriver webDriver;
    public AbstractUiFragment(WebDriver driver) {
        this.webDriver = driver;
    }
    public void waitUntil(ExpectedCondition<?> condition) {
        PageWait.wait(webDriver).until(condition);
    }
    protected void waitTextInElement(WebElement element, String expected) {
        waitVisibilityOfElement(element);
        waitUntil(textToBePresentInElement(element, expected));
    }

    protected void waitVisibilityOfElement(WebElement element) {
        waitUntil(visibilityOf(element));
    }
}
