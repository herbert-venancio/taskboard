package objective.taskboard.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class IssueErrorToast extends AbstractUiFragment {

    @FindBy(id="toastIssueError")
    private WebElement toast;

    public IssueErrorToast(WebDriver webDriver) {
        super(webDriver);
    }

    public IssueErrorToast assertVisible() {
        waitVisibilityOfElement(toast);
        return this;
    }

    public IssueErrorToast assertNotVisible() {
        waitInvisibilityOfElement(toast);
        return this;
    }

    public IssueDetails clickOpen(int positionFromBottomToTop) {
        WebElement dismiss = toast.findElement(By.cssSelector(".issue-error:nth-child(" + positionFromBottomToTop + ") .issue-error__open"));
        dismiss.click();
        return new IssueDetails(webDriver);
    }

    public IssueErrorToast clickDismiss(int positionFromBottomToTop) {
        WebElement dismiss = toast.findElement(By.cssSelector(".issue-error:nth-child(" + positionFromBottomToTop + ") .issue-error__close"));
        dismiss.click();
        return this;
    }

}
