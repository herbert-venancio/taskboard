package objective.taskboard.it.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;

public class ClusterAlgorithmFormComponent extends AbstractComponent {

    public final InputComponent startDateInput;
    public final InputComponent endDateInput;
    public final ErrorMessagesComponent startDateErrorMessages;
    public final ErrorMessagesComponent endDateErrorMessages;

    public ClusterAlgorithmFormComponent(WebDriver driver, By by) {
        super(driver, by);
        startDateInput = new InputComponent(driver, new ByChained(by, By.cssSelector("input[name='startDate']")));
        endDateInput = new InputComponent(driver, new ByChained(by, By.cssSelector("input[name='endDate']")));
        startDateErrorMessages = new ErrorMessagesComponent(driver, new ByChained(by, By.cssSelector("obj-label-field[label='Start date'] obj-error-messages")));
        endDateErrorMessages = new ErrorMessagesComponent(driver, new ByChained(by, By.cssSelector("obj-label-field[label='End date'] obj-error-messages")));
    }

    public CheckboxComponent getProjectCheckbox(String projectName) {
        return new CheckboxComponent(webDriver, new ByChained(componentSelector, By.cssSelector(".checkbox-wrapper[title=\""+ projectName +"\"")));
    }

}
