package objective.taskboard.it;

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