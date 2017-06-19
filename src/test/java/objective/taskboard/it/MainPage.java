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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MainPage {
    @FindBy(css=".nameButton.user-account")
    private WebElement userLabelButton;

    
    private WebDriver webDriver;

    public MainPage(WebDriver driver) {
        this.webDriver = driver;
    }
    
    public static MainPage produce(WebDriver webDriver) {
        return PageFactory.initElements(webDriver, MainPage.class);
    }
    
    public static MainPage to(WebDriver webDriver) {
        webDriver.get(UIConfig.getSiteBase()+"/");
        return PageFactory.initElements(webDriver, MainPage.class);
    }

    public void waitUserLabelToBe(String expected) {
        waitTextInElement(userLabelButton, expected);
    }
    
    
    private void waitTextInElement(WebElement element, String expected) {
        PageWait.wait(webDriver).until(ExpectedConditions.visibilityOf(element));
        PageWait.wait(webDriver).until(ExpectedConditions.textToBePresentInElement(element, expected));        
    }
}
