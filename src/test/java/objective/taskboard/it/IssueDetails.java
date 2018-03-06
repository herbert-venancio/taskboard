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

import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

class IssueDetails extends AbstractUiFragment {

    WebElement issueDetailRoot;
    public IssueDetails(WebDriver driver) {
        super(driver);
        issueDetailRoot = webDriver.findElement(By.cssSelector("paper-dialog.issue-detail"));
    }

    public IssueDetails assignToMe() {
        assertIsOpened();
        WebElement assignButton = issueDetailRoot.findElement(By.id("assignButton"));
        waitForClick(assignButton);
        return this;
    }

    public IssueDetails assertAssigneeIs(String name) {
        assertIsOpened();
        WebElement assigneeElement = getElementWhenItExists(By.className("assignee"));
        waitTextInElement(assigneeElement, name);
        return this;
    }

    public void assertIsClosed() {
        waitUntil(attributeToBe(issueDetailRoot, "data-status", "closed"));
        waitInvisibilityOfElement(issueDetailRoot);
    }

    public IssueDetails transitionClick(String transitionName) {
        assertIsOpened();
        waitUntilElementExists(By.cssSelector("[data-transition-name='"+transitionName+"']"));
        WebElement transitionButton = issueDetailRoot.findElement(By.cssSelector("[data-transition-name='"+transitionName+"']"));
        waitForClick(transitionButton);
        return this;
    }

    public IssueDetails confirm() {
        WebElement confirmationModal = webDriver.findElement(By.id("confirmModal"));
        waitVisibilityOfElement(confirmationModal);
        WebElement confirmButton = confirmationModal.findElement(By.id("confirm"));
        waitForClick(confirmButton);
        assertIsClosed();
        return this;
    }

    public IssueDetails closeDialog() {
        assertIsOpened();
        WebElement close = issueDetailRoot.findElement(By.className("buttonClose"));
        waitForClick(close);
        assertIsClosed();
        return this;
    }

    public IssueDetails assertRefreshWarnIsOpen() {
        assertIsOpened();
        waitUntilElementExists(By.id("glasspane-updated"));
        return this;
    }

    public IssueDetails clickOnRefreshWarning() {
        WebElement glasspane = webDriver.findElement(By.id("glasspane-updated"));
        waitForClick(glasspane);
        return this;
    }

    public IssueDetails assertDeleteWarnIsOpen() {
        assertIsOpened();
        waitUntilElementExists(By.id("glasspane-deleted"));
        return this;
    }

    public IssueDetails clickOnDeleteWarning() {
        WebElement glasspane = webDriver.findElement(By.id("glasspane-deleted"));
        waitForClick(glasspane);
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
        WebElement closeError = error.findElement(By.className("message-box__close"));
        waitForClick(closeError);
        waitInvisibilityOfElement(error);
        return this;
    }

    public IssueDetails assertClassOfService(String classOfServiceExpected) {
        assertIsOpened();
        WebElement classOfServiceValue = issueDetailRoot.findElement(By.id("classOfServiceValue"));
        waitTextInElement(classOfServiceValue, classOfServiceExpected);
        return this;
    }

    public IssueDetails assertColor(String colorExpected) {
        assertIsOpened();
        waitAttributeValueInElement(issueDetailRoot, "background-color", colorExpected);
        return this;
    }

    private void assertIsOpened() {
        waitAttributeValueInElement(issueDetailRoot, "data-status", "opened");
    }
}