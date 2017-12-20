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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

class IssueDetails extends AbstractUiFragment {
    WebElement issueDetailRoot;
    public IssueDetails(WebDriver driver) {
        super(driver);
        issueDetailRoot = webDriver.findElement(By.cssSelector("paper-card.issue-detail"));
    }
    
    public IssueDetails assignToMe() {
        waitVisibilityOfElement(issueDetailRoot);
        WebElement assignButton = issueDetailRoot.findElement(By.id("assignButton"));
        waitVisibilityOfElement(assignButton);
        assignButton.click();
        return this;
    }
    
    public IssueDetails assertAssigneeIs(String name) {
        waitUntilElementExists(By.className("assignee"));
        WebElement assigneeElement = issueDetailRoot.findElement(By.className("assignee"));
        waitTextInElement(assigneeElement, name);
        return this;
    }
    
    public void assertIsHidden() {
        waitInvisibilityOfElement(issueDetailRoot);
    }

    public IssueDetails transitionClick(String transitionName) {
        waitVisibilityOfElement(issueDetailRoot);
        waitUntilElementExists(By.cssSelector("[data-transition-name='"+transitionName+"']"));
        WebElement transitionButton = issueDetailRoot.findElement(By.cssSelector("[data-transition-name='"+transitionName+"']"));
        waitVisibilityOfElement(transitionButton);
        transitionButton.click();
        
        return this;
    }        
    
    public IssueDetails confirm() {
        WebElement confirmationModal = webDriver.findElement(By.id("confirmModal"));
        waitVisibilityOfElement(confirmationModal);
        WebElement confirmButton = confirmationModal.findElement(By.id("confirm"));
        waitVisibilityOfElement(confirmButton);
        confirmButton.click();
        return this;
    }

    public IssueDetails closeDialog() {
        issueDetailRoot.findElement(By.className("buttonClose")).click();
        return this;
    }

    public IssueDetails assertRefreshWarnIsOpen() {
        waitUntilElementExists(By.className("glasspane"));
        return this;
    }

    public IssueDetails clickOnWarning() {
        WebElement glasspane = webDriver.findElement(By.className("glasspane"));
        waitVisibilityOfElement(glasspane);
        glasspane.click();
        return this;
    }

    public IssueDetails assertInvalidTeamWarnIsVisible() {
        WebElement icon = issueDetailRoot.findElement(By.cssSelector(".assignee .icon"));
        waitVisibilityOfElement(icon);
        return this;
    }

    public IssueDetails assertInvalidTeamWarnIsInvisible() {
        waitUntilElementNotExists(By.cssSelector(".assignee .icon"));
        return this;
    }

    public IssueDetails assertHasError(String text) {
        WebElement error = getElementWhenItExists(By.className("message-box--error"));
        waitVisibilityOfElement(error);
        WebElement errorText = error.findElement(By.className("message-box__message"));
        waitTextInElement(errorText, text);
        return this;
    }

    public IssueDetails closeError() {
        WebElement error = getElementWhenItExists(By.className("message-box--error"));
        error.findElement(By.className("message-box__close")).click();
        waitInvisibilityOfElement(error);
        return this;
    }

    public IssueDetails assertClassOfService(String classOfServiceExpected) {
        waitVisibilityOfElement(issueDetailRoot);
        WebElement classOfServiceValue = issueDetailRoot.findElement(By.id("classOfServiceValue"));
        waitTextInElement(classOfServiceValue, classOfServiceExpected);
        return this;
    }

    public IssueDetails assertColor(String colorExpected) {
        WebElement paperDialog = webDriver.findElement(By.cssSelector("paper-dialog.issue-detail"));
        waitVisibilityOfElement(paperDialog);
        waitAttributeValueInElement(paperDialog, "background-color", colorExpected);
        return this;
    }
}