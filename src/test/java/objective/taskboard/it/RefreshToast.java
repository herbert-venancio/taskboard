package objective.taskboard.it;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
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

public class RefreshToast extends AbstractUiFragment {
    @FindBy(id="toastIssueUpdated")
    private WebElement issueToast;
    
    @FindBy(id="toggleFilterChangedIssues")
    private WebElement toggleFilterChangedIssues;
    
    @FindBy(id="dismissToast")
    private WebElement dismissToast;
    
    
    public RefreshToast(WebDriver webDriver) {
        super(webDriver);
    }
    
    public void assertVisible() {
        waitVisibilityOfElement(issueToast);
    }

    public void toggleShowHide() {
        waitVisibilityOfElement(toggleFilterChangedIssues);
        toggleFilterChangedIssues.click();
    }

    public void assertNotVisible() {
        waitInvisibilityOfElement(issueToast);
    }

    public void dismiss() {
        waitVisibilityOfElement(dismissToast);
        dismissToast.click();
    }
}