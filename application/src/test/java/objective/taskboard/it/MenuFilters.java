package objective.taskboard.it;

import objective.taskboard.it.basecluster.BaseClusterSearchPage;
import objective.taskboard.testUtils.ProjectInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.join;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.openqa.selenium.By.cssSelector;

public class MenuFilters extends AbstractUiFragment {

    private static final String CONFIG_TEAMS_SELECTOR = "config-teams";
    private static final String CONFIG_BASE_CLUSTER_SELECTOR = "config-base-clusters";
    private static final String CONFIG_ITEM_TITLE = ".config-item-title";

    @FindBy(tagName = "card-field-filters")
    private WebElement cardFieldFilters;

    @FindBy(tagName = "card-field-filter")
    private List<WebElement> listOfCardFieldFilter;

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
        WebElement ironCollapse = cardFieldFilters.findElement(cssSelector("iron-collapse"));
        waitAttributeValueInElementContains(ironCollapse, "class", "iron-collapse-opened");
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

    public MenuFilters openCardFieldFilter(String cardFieldFilterName) {
        WebElement projectCardFieldFilter = listOfCardFieldFilter
                .stream()
                .filter(cardFieldFilter -> cardFieldFilter.findElement(cssSelector(CONFIG_ITEM_TITLE)).getText().equals(cardFieldFilterName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Card field Filter not found : " + cardFieldFilterName));
        waitForClick(projectCardFieldFilter.findElement(cssSelector(CONFIG_ITEM_TITLE)));
        WebElement ironCollapse = projectCardFieldFilter.findElement(cssSelector("iron-collapse"));
        waitAttributeValueInElementContains(ironCollapse, "class", "iron-collapse-opened");

        return this;
    }
    public MenuFilters assertProjectsAreSelected(String... expectedSelectedProjects) {
        final String unselectedClass = "icon";

        waitAssertEquals(join(expectedSelectedProjects, "\n"), () -> {
            List<String> listOfSelectedProjects = new ArrayList<>();

            WebElement projectCardFieldFilter = listOfCardFieldFilter.stream()
                    .filter(filter -> filter.findElement(cssSelector(".config-item-title")).getText().equals("Project"))
                    .findFirst().orElseThrow(IllegalStateException::new);

            projectCardFieldFilter.findElements(cssSelector("filter-field-value")).forEach(filterFieldValue -> {
                WebElement elWithSelectedClass = filterFieldValue.findElement(cssSelector("paper-button"));
                String[] classes = elWithSelectedClass.getAttribute("class").split(" ");
                if (!asList(classes).contains(unselectedClass))
                    listOfSelectedProjects.add(filterFieldValue.getText());
            });

            return listOfSelectedProjects.stream().collect(joining("\n"));

        });
        return this;
    }

}
