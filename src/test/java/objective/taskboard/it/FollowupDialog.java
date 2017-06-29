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

import static org.openqa.selenium.support.PageFactory.initElements;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FollowupDialog extends AbstractUiFragment {
    private WebElement toggleCheckbox;
    
    @FindBy(css=".followup-button")
    private WebElement followupButton;
    
    @FindBy(id="followupdialog")
    private WebElement dialog;
    
    
    public static FollowupDialog open(WebDriver webDriver) {
        return initElements(webDriver, FollowupDialog.class).open();
    }
    
    private FollowupDialog open() {
        followupButton.click();
        waitVisibilityOfElement(dialog);
        toggleCheckbox = dialog.findElement(By.id("toggleAll"));
        return this;
    }

    public FollowupDialog(WebDriver driver) {
        super(driver);
    }

    public FollowupDialog toggleCheckAll() {
        toggleCheckbox.click();
        waitUntil(ExpectedConditions.attributeToBe(toggleCheckbox, "checked", "true"));
        return this;
    }

    public FollowupDialog assertAllChecked() {
        final WebElement listOfProjects = dialog.findElement(By.cssSelector(".followup-dialog #listOfProjects"));
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                List<WebElement> projectCheckBoxes = listOfProjects.findElements(By.cssSelector("paper-checkbox"));
                List<WebElement> checkedProjects = listOfProjects.findElements(By.cssSelector("paper-checkbox[aria-checked='true']"));
                return projectCheckBoxes.size() == checkedProjects.size(); 
            }
        });
        return this;
    }

}
