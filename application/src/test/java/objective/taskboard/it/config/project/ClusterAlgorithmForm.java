package objective.taskboard.it.config.project;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;

import objective.taskboard.it.components.ErrorMessagesComponent;
import objective.taskboard.it.components.InputComponent;

public class ClusterAlgorithmForm {

    public final InputComponent startDateInput;
    public final InputComponent endDateInput;
    public final ErrorMessagesComponent startDateErrorMessages;
    public final ErrorMessagesComponent endDateErrorMessages;

    public ClusterAlgorithmForm(WebDriver driver, By by) {
        startDateInput = new InputComponent(driver, new ByChained(by, By.cssSelector("input[name='startDate']")));
        endDateInput = new InputComponent(driver, new ByChained(by, By.cssSelector("input[name='endDate']")));
        startDateErrorMessages = new ErrorMessagesComponent(driver, new ByChained(by, By.cssSelector("obj-label-field[label='Start date'] obj-error-messages")));
        endDateErrorMessages = new ErrorMessagesComponent(driver, new ByChained(by, By.cssSelector("obj-label-field[label='End date'] obj-error-messages")));
    }

}
