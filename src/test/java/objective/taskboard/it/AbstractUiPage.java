package objective.taskboard.it;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class AbstractUiPage {
    protected WebDriver webDriver;
    public AbstractUiPage(WebDriver driver) {
        this.webDriver = driver;
    }
    public void waitUntil(ExpectedCondition<?> condition) {
        PageWait.wait(webDriver).until(condition);
        
    }
    protected void waitTextInElement(WebElement element, String expected) {
        waitUntil(visibilityOf(element));
        waitUntil(textToBePresentInElement(element, expected));        
    }
}
