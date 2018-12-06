package objective.taskboard.it;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

class TestIssue extends AbstractUiFragment {
    private WebElement issueElement;

    public TestIssue(WebDriver driver, String issueKey) {
        super(driver);
        this.issueElement = getIssueByKey(issueKey);
    }

    public TestIssue click() {
        waitForClick(issueElement);
        return this;
    }

    public TestIssue select() {
        waitForClickHoldingAKey(issueElement, Keys.META);
        return this;
    }

    public TestIssue moveToTop() {
        WebElement moveToTopButton = issueElement.findElement(By.className("arrow-box"));
        waitForClick(moveToTopButton);
        return this;
    }

    public void enableHierarchicalFilter() {
        Actions builder = new Actions(webDriver);
        builder.moveToElement(issueElement).build().perform();
        WebElement applyFilterButton = issueElement.findElement(By.cssSelector("[alt='Apply Filter']"));
        waitForClick(applyFilterButton);
    }

    public IssueDetails issueDetails() {
        return new IssueDetails(webDriver);
    }

    public TestIssue assertHasFirstAssignee() {
        WebElement assignee1 = issueElement.findElement(By.id("assignee0"));
        waitVisibilityOfElement(assignee1);
        return this;
    }

    public TestIssue assertHasSecondAssignee() {
        WebElement assignee2 = issueElement.findElement(By.id("assignee1"));
        waitVisibilityOfElement(assignee2);
        return this;
    }

    public void dragOver(String targetKey) {
        waitVisibilityOfElement(issueElement);
        Actions actions = new Actions(webDriver);
        WebElement targetIssue = new TestIssue(webDriver, targetKey).issueElement.findElement(By.className("module"));
        waitVisibilityOfElement(targetIssue);
        actions
            .clickAndHold(issueElement)
            .moveToElement(targetIssue)
            .release(targetIssue)
            .perform();
    }

    public TestIssue assertHasError(boolean hasError) {
        WebElement errorElement = getElementWhenItExists(By.className("condition-icon--has-error"));
        if (hasError)
            waitVisibilityOfElement(errorElement);
        else
            waitInvisibilityOfElement(errorElement);
        return this;
    }

    public TestIssue assertIsUpdating(boolean isUpdating) {
        WebElement updatingElement = getElementWhenItExists(By.className("condition-icon--updating"));
        if (isUpdating)
            waitVisibilityOfElement(updatingElement);
        else
            waitInvisibilityOfElement(updatingElement);
        return this;
    }

    public TestIssue assertCardColor(String colorExpected) {
        waitVisibilityOfElement(issueElement);
        WebElement issueCard = issueElement.findElement(By.id("issueCard"));
        waitAttributeValueInElement(issueCard, "background-color", colorExpected);
        return this;
    }

    private WebElement getIssueByKey(String issueKey) {
        List<WebElement> issues = getElementsWhenTheyExists(By.cssSelector("issue-item#"+issueKey));

        if (issues.size() > 1)
            throw new IllegalArgumentException("More than a single match was found");

        if (issues.size() == 0)
            throw new IllegalArgumentException("Issue " + issueKey + " not found.");

        return issues.get(0);
    }

}