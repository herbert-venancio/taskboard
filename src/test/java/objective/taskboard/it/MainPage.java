package objective.taskboard.it;

import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;

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
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MainPage extends AbstractUiPage {
    @FindBy(css=".nameButton.user-account")
    private WebElement userLabelButton;
    
    @FindBy(id="searchIssues")
    private WebElement searchIssuesInput;

    
    public MainPage(WebDriver webDriver) {
        super(webDriver);
    }
    
    public static MainPage produce(WebDriver webDriver) {
        return PageFactory.initElements(webDriver, MainPage.class);
    }
    
    public static AbstractUiPage to(WebDriver webDriver) {
        webDriver.get(UIConfig.getSiteBase()+"/");
        return PageFactory.initElements(webDriver, MainPage.class);
    }

    public void waitUserLabelToBe(String expected) {
        waitTextInElement(userLabelButton, expected);
    }
    
    public void typeSearch(String searchValue) {
        searchIssuesInput.sendKeys(searchValue);
        waitUntil(textToBePresentInElementValue(searchIssuesInput, searchValue));
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
}
