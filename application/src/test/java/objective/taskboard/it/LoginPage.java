package objective.taskboard.it;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends AbstractUiFragment {
    
    @FindBy(css = "#username input")
    private WebElement username;

    @FindBy(css = "#password input")
    private WebElement password;

    @FindBy(id = "login")
    private WebElement submit;
    
    @FindBy(css = ".incorrect")
    private WebElement validationMessage;

    public LoginPage(WebDriver webDriver) {
        super(webDriver);
    }

    public LoginPage login(String username, String password) {
        waitVisibilityOfElement(this.username);
        
        this.username.sendKeys(username);
        this.password.sendKeys(password);
        this.submit.click();
        
        return this;
    }   

    public static LoginPage to(WebDriver webDriver) {
        webDriver.get(AbstractUIIntegrationTest.getSiteBase()+"/login");
        return PageFactory.initElements(webDriver, LoginPage.class);
    }

    public void assertValidationMessage(String message) {
        waitTextInElement(validationMessage, message);
    }
}
