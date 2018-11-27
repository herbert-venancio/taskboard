package objective.taskboard.it;

import static java.util.stream.Collectors.joining;
import static objective.taskboard.it.AbstractIntegrationTest.getAppBaseUrl;
import static objective.taskboard.it.components.SearchComponent.SEARCH_TAG;
import static org.apache.commons.lang3.StringUtils.join;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.support.PageFactory.initElements;

import java.util.List;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.components.SearchComponent;

public class TeamsPage extends AbstractAppUiFragment {

    private static final String PAGE_TAG = "tb-teams ";

    private static final String NAME_SELECTOR = "td:nth-child(1)";
    private static final String MANAGER_SELECTOR = "td:nth-child(2)";
    private static final String MEMBERS_SELECTOR = "td:nth-child(3)";

    @FindBy(css=PAGE_TAG +" #tb-page-title")
    private WebElement pageTitle;

    @FindBy(css=PAGE_TAG +" #tb-teams-list")
    private WebElement teamsList;

    private SearchComponent filterTeams;

    public TeamsPage(WebDriver webDriver) {
        super(webDriver);
        filterTeams = new SearchComponent(webDriver, cssSelector(PAGE_TAG + SEARCH_TAG));

        initElements(webDriver, this);
        assertPageIsOpen();
    }

    private TeamsPage assertPageIsOpen() {
        waitTextInElement(pageTitle, "Teams");
        waitPageLoaderBeHide();
        return this;
    }

    public TeamsPage filterTeams(String value) {
        filterTeams.search(value);
        return this;
    }

    public TeamsPage assertVisibleTeams(String... expectedTeamsGroups) {
        waitAssertEquals(join(expectedTeamsGroups, "\n"), () -> {
            List<WebElement> groups = teamsList.findElements(cssSelector("tbody tr"));
            return IntStream
                    .range(0, groups.size())
                    .mapToObj(index -> {
                        WebElement tdName = getChildElementWhenExists(teamsList, getGroupElementSelector(index, NAME_SELECTOR));
                        WebElement tdManager = getChildElementWhenExists(teamsList, getGroupElementSelector(index, MANAGER_SELECTOR));
                        WebElement tdMembers = getChildElementWhenExists(teamsList, getGroupElementSelector(index, MEMBERS_SELECTOR));
                        return tdName.getText() +" | "+ tdManager.getText() +" | "+ tdMembers.getText();
                    })
                    .collect(joining("\n"));
        });
        return this;
    }

    public TeamPage selectTeam(String teamName) {
        filterTeams(teamName);
        waitForClick(cssSelector(PAGE_TAG +" tbody tr[data-team-name=\""+ teamName +"\"] td:first-child"));
        return new TeamPage(webDriver, teamName);
    }

    private By getGroupElementSelector(int groupIndex, String elementSelector) {
        return cssSelector(PAGE_TAG +" tbody tr:nth-child(" + (groupIndex + 1) + ") " + elementSelector);
    }

    public static String getPageUrl() {
        return getAppBaseUrl() + "teams";
    }


}
