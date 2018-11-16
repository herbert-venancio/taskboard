package objective.taskboard.it.config.project;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;

import objective.taskboard.it.components.InputComponent;

public class ClusterAlgorithmForm {

    private final WebDriver driver;
    private final InputComponent startDateInput;
    private final InputComponent endDateInput;

    public ClusterAlgorithmForm(WebDriver driver, By by) {
        this.driver = driver;
        startDateInput = new InputComponent(driver, new ByChained(by, By.cssSelector("input[name='startDate']")));
        endDateInput = new InputComponent(driver, new ByChained(by, By.cssSelector("input[name='endDate']")));
    }

    public ClusterAlgorithmForm setStartDate(String date) {
        startDateInput.setValue(date);
        return this;
    }

    public ClusterAlgorithmForm setEndDate(String date) {
        endDateInput.setValue(date);
        return this;
    }
}
