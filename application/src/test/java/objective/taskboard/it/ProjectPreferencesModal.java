package objective.taskboard.it;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

public class ProjectPreferencesModal extends AbstractUiFragment {

    public static String PROJECT_01 = "Project 1";

    @FindBy(css= ".modal-project-preferences")
    private WebElement modalProjectPreferences;

    @FindBy(css= ".tb-paper-checkbox")
    private List<WebElement> listOfProjects;

    @FindBy(css= "#submitPreferencesButton")
    private WebElement submitPreferencesButton;

    public static ProjectPreferencesModal produce(WebDriver webDriver) {
        return initElements(webDriver, ProjectPreferencesModal.class);
    }

    public ProjectPreferencesModal(WebDriver driver) { super(driver); }

    public ProjectPreferencesModal waitUntilModalIsReady(){
        waitTrue(() -> isElementVisibleAndExists(cssSelector("project-preferences-modal .alert"))
                || isElementVisibleAndExists(cssSelector("project-preferences-modal .projects-list")));
        return this;
    }

    public ProjectPreferencesModal selectProjects(String... projects) {

        asList(projects).stream()
            .map(projToSelect -> {
                waitTrue(() -> listOfProjects.stream().anyMatch(availableProj -> availableProj.getText().equals(projToSelect)));
                return listOfProjects.stream()
                        .filter(availableProj -> availableProj.getText().equals(projToSelect))
                        .findFirst().orElseThrow(IllegalStateException::new) ;
            })
            .forEach(projElToSelect -> waitForClick(projElToSelect));

        return this;
    }

    public MainPage submitSelectedProjects() {
        waitForClick(submitPreferencesButton);
        return initElements(webDriver, MainPage.class);
    }

}
