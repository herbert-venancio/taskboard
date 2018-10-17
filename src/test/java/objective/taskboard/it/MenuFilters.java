package objective.taskboard.it;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.basecluster.BaseClusterSearchPage;
import objective.taskboard.testUtils.ProjectInfo;

public class MenuFilters extends AbstractUiFragment {

    private static final String CONFIG_TEAMS_SELECTOR = "config-teams";
    private static final String CONFIG_BASE_CLUSTER_SELECTOR = "config-base-clusters";

    @FindBy(tagName = "card-field-filters")
    private WebElement cardFieldFilters;

    @FindBy(css = "card-field-filter .config-item-title")
    private List<WebElement> cardFieldFiltersHeaders;

    @FindBy(tagName = "filter-field-value")
    private List<WebElement> filterFieldsValues;

    @FindBy(tagName = "config-projects")
    private WebElement projectsConfigurationButton;

    @FindBy(css = ".config-projects.config-item-project")
    private List<WebElement> projectsConfigurationItem;

    @FindBy(css = "config-table")
    private WebElement boardFeatures;

    @FindBy(css = CONFIG_TEAMS_SELECTOR)
    private WebElement teamsConfigurationButton;

    @FindBy(css = CONFIG_BASE_CLUSTER_SELECTOR)
    private WebElement baseClusterConfigurationButton;

    public MenuFilters(WebDriver webDriver) {
        super(webDriver);
    }

    public MenuFilters openCardFieldFilters() {
        waitForClick(cardFieldFilters);
        return this;
    }

    public MenuFilters openProjectsConfiguration() {
        waitForClick(projectsConfigurationButton);
        return this;
    }

    public MenuFilters assertTeamsConfigurationExistenceBe(boolean exists) {
        waitElementExistenceAndVisibilityIs(exists, By.cssSelector(CONFIG_TEAMS_SELECTOR));
        return this;
    }

    public TeamsPage openTeamsConfiguration() {
        waitForClick(teamsConfigurationButton);
        return new TeamsPage(webDriver);
    }

    public MenuFilters clickCheckAllFilter(String filterName) {
        WebElement checkAll = cardFieldFiltersHeaders.stream()
            .filter(cardFieldFilterHeader -> filterName.equals(cardFieldFilterHeader.getText()))
            .map(cardFieldFilterHeader -> cardFieldFilterHeader.findElement(By.id("checkAll")))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Element \"checkAll\" of " + filterName + " filter  not found"));

        waitForClick(checkAll);
        return this;
    }

    public ProjectConfigurationDialog openProjectConfigurationModal(ProjectInfo projectInfo) {
        WebElement projectItemButton = projectsConfigurationItem.stream()
            .filter(el -> projectInfo.key.equals(el.getText()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Element  for project key " + projectInfo.key + " filter  not found"));

        waitForClick(projectItemButton);

        return new ProjectConfigurationDialog(webDriver, projectInfo).assertIsOpen();
    }

    public MenuFilters clickFilterFieldValue(String cardFieldFilterType, String filterFieldValueName) {
        WebElement cardFieldFilterToClick = cardFieldFiltersHeaders.stream()
            .filter(cardFieldFilterHeader -> cardFieldFilterType.equals(cardFieldFilterHeader.getText()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(cardFieldFilterType + " filter  not found"));

        waitForClick(cardFieldFilterToClick);

        WebElement filterFieldValueToClick = filterFieldsValues.stream()
            .filter(filterFieldValue -> filterFieldValueName.equals(filterFieldValue.getText()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(filterFieldValueName + " filter  not found"));

        waitForClick(filterFieldValueToClick);
        return this;
    }

    public void toggleLaneVisibilityAndReload(String laneName, MainPage mainPage) {
        toggleElementVisibility(LaneFragment.laneSelector(laneName), () -> {
            waitForClick(boardFeatures);
            waitForClick(boardFeatures.findElement(By.cssSelector("config-level .config-item-title")));
            WebElement laneConf = boardFeatures.findElements(By.cssSelector(".level-title")).stream()
                .filter(el -> laneName.equals(el.getText()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("There's no lane with given name: " + laneName));
            waitForClick(laneConf.findElement(By.cssSelector(".visibility-button")));
        });
        mainPage.reload();
    }

    public void closeMenuFilters() {
        waitForClick(webDriver.findElement(By.id("scrim")));
        waitInvisibilityOfElement(cardFieldFilters);
    }

    public MenuFilters assertBaseClusterButtonVisible() {
        waitElementExistenceAndVisibilityIs(true, By.cssSelector(CONFIG_BASE_CLUSTER_SELECTOR));
        return this;
    }

    public MenuFilters assertBaseClusterButtonNotVisible() {
        waitElementExistenceAndVisibilityIs(false, By.cssSelector(CONFIG_BASE_CLUSTER_SELECTOR));
        return this;
    }

    public BaseClusterSearchPage openBaseClusterSearch() {
        waitForClick(baseClusterConfigurationButton);
        return new BaseClusterSearchPage(webDriver);
    }
}
