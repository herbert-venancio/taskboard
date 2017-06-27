package objective.taskboard.it;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MainPage extends AbstractUiPage {
    @FindBy(css=".nameButton.user-account")
    private WebElement userLabelButton;

    @FindBy(id = "searchIssues")
    private WebElement searchIssuesInput;

    @FindBy(id = "searchRelease")
    private WebElement searchReleaseDropdown;

    @FindBy(css = ".menuLink")
    private WebElement menuFiltersButton;

    @FindBy(tagName = "aspects-filter")
    private WebElement aspectsFilterButton;

    public MainPage(WebDriver webDriver) {
        super(webDriver);
    }
    
    public static MainPage produce(WebDriver webDriver) {
        return PageFactory.initElements(webDriver, MainPage.class);
    }
    
    public static AbstractUiPage to(WebDriver webDriver) {
        webDriver.get(AbstractUIIntegrationTest.getSiteBase()+"/");
        return PageFactory.initElements(webDriver, MainPage.class);
    }

    public void waitUserLabelToBe(String expected) {
        waitTextInElement(userLabelButton, expected);
    }
    
    public void typeSearch(String searchValue) {
        searchIssuesInput.sendKeys(searchValue);
        waitUntil(textToBePresentInElementValue(searchIssuesInput, searchValue));
    }

    public MainPage openMenuFilters() {
        waitVisibilityOfElement(menuFiltersButton);
        menuFiltersButton.click();
        return this;
    }

    public MainPage openAspectsFilter() {
        waitVisibilityOfElement(aspectsFilterButton);
        aspectsFilterButton.click();
        return this;
    }

    public MainPage clickCheckAllProjectFilter() {
        WebElement checkAllProject = webDriver
            .findElements(By.cssSelector(".aspect-item-filter.config-item-title")).stream()
            .filter(webEl -> webEl.getText().equals("Project"))
            .map(webEl -> webEl.findElement(By.id("checkAll")))
            .findFirst().orElse(null);
        if (checkAllProject == null)
            throw new IllegalArgumentException("Element \"checkAll\" of project filter not found");
        checkAllProject.click();
        return this;
    }

    public MainPage assertLabelRelease(String expected) {
        waitTextInElement(searchReleaseDropdown, expected);
        return this;
    }

    public void assertVisibleIssues(String ... expectedIssueKeyList) {
        waitUntil(ExpectedConditions.numberOfElementsToBe(By.cssSelector("paper-material.issue"), expectedIssueKeyList.length));
        List<WebElement> findElements = webDriver.findElements(By.cssSelector("paper-material.issue"));
        ArrayList<String> actualIssueKeyList = new ArrayList<String>(); 
        for (WebElement webElement : findElements) 
            actualIssueKeyList.add( webElement.findElement(By.cssSelector(".key.issue-item")).getText().trim());
        Arrays.sort(expectedIssueKeyList);
        Collections.sort(actualIssueKeyList);
        
        assertEquals(join(expectedIssueKeyList,"\n"), join(actualIssueKeyList,"\n"));
    }

    public TestIssue issue(String issueKey) {
        List<WebElement> elements = webDriver.findElements(By.cssSelector("paper-material.issue")).stream()
            .filter(webEl -> hasChildThatMatches(webEl, By.cssSelector("[data-issue-key='"+issueKey+"']")))
            .collect(toList());
        
        if (elements.size() > 1)
            throw new IllegalArgumentException("More than a single match was found");
        if (elements.size() == 0)
            return null;
        return new TestIssue(elements.get(0));
    }
    
    private Boolean hasChildThatMatches(WebElement webEl, By selector) {
        try {
            webEl.findElement(selector);
            return true;
        }catch (NoSuchElementException e) {
            return false;
        }
    }

    class TestIssue {
        private WebElement webElement;

        public TestIssue(WebElement webElement) {
            this.webElement = webElement;
        }
        
        public void click() {
            webElement.click();
        }

        public void enableHierarchicalFilter() {
            Actions builder = new Actions(webDriver);
            builder.moveToElement(webElement).build().perform();
            WebElement applyFilterButton = webElement.findElement(By.cssSelector("[alt='Apply Filter']"));
            waitUntil(ExpectedConditions.visibilityOf(applyFilterButton));
            applyFilterButton.click();;
        }
    }
}
