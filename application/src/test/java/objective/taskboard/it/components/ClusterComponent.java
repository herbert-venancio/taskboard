package objective.taskboard.it.components;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

public class ClusterComponent extends AbstractComponent {

    public static final String CLUSTER_TAG = "tb-cluster";

    public ClusterComponent(WebDriver webDriver, By componentSelector) {
        super(webDriver, componentSelector);
    }

    public void setEffort(String issueType, String size, String effort) {
        setInputValue(getInputByIssueTypeAndSize(issueType, "effort", size), effort);
    }

    public void assertEffort(String issueType, String size, String expectedEffort) {
        waitAttributeValueInElement(getInputByIssueTypeAndSize(issueType, "effort", size), "value", expectedEffort);
    }

    public void setCycle(String issueType, String size, String cycle) {
        setInputValue(getInputByIssueTypeAndSize(issueType, "cycle", size), cycle);
    }

    public void assertCycle(String issueType, String size, String expectedCycle) {
        waitAttributeValueInElement(getInputByIssueTypeAndSize(issueType, "cycle", size), "value", expectedCycle);
    }

    public String getCycleValue(String issueType, String size) {
        WebElement element = getInputByIssueTypeAndSize(issueType, "cycle", size);
        return element.getAttribute("value");
    }

    public void selectNewCycleValue(String issueType) {
        CheckboxComponent checkbox = new CheckboxComponent(webDriver, new ByChained(componentSelector, 
                cssSelector("[data-issue-type=\"" + issueType + "\"] input[name=\"accept-cycle-changes\"]")));
        checkbox.select();
    }

    public void assertCurrentValueNewValueIsShown(String issueType, String tsize) {
        getInputByIssueTypeAndSize(issueType, "original-effort", tsize);
        getInputByIssueTypeAndSize(issueType, "effort", tsize);
        getInputByIssueTypeAndSize(issueType, "original-cycle", tsize);
        getInputByIssueTypeAndSize(issueType, "cycle", tsize);
    }

    private WebElement getInputByIssueTypeAndSize(String issueType, String input, String size) {
        return getChildElementWhenExists(component(), id(issueType + "-" + input + "-" + size));
    }

}
