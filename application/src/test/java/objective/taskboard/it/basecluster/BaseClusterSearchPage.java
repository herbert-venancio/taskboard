package objective.taskboard.it.basecluster;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.it.AbstractIntegrationTest.getAppBaseUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.support.PageFactory.initElements;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import objective.taskboard.it.AbstractAppUiFragment;

public class BaseClusterSearchPage extends AbstractAppUiFragment {

    private static final String PAGE_TAG = "tb-base-cluster-search";

    @FindBy(css=PAGE_TAG +" #tb-page-title")
    private WebElement pageTitle;

    @FindBy(css=PAGE_TAG +" #tb-base-clusters-list tbody")
    private WebElement clusterTableBody;

    @FindBy(css=PAGE_TAG +" #create-blase-cluster")
    private WebElement addBaseClusterButton;

    public BaseClusterSearchPage(WebDriver webDriver) {
        super(webDriver);
        initElements(webDriver, this);
        assertPageIsOpen();
    }

    public static String getPageUrl() {
        return getAppBaseUrl() + "base-cluster-search";
    }

    public BaseClusterSearchPage assertBaseClusters(String... baseClusters) {
        List<WebElement> rows = getClusterRows();
        List<String> actualClusters = rows.stream().map(e -> e.getText()).collect(toList());

        assertBaseClusterItems(actualClusters, Arrays.asList(baseClusters));
        return this;
    }

    public BaseClusterPage newBaseCluster() {
        waitForClick(addBaseClusterButton);
        return new BaseClusterPage(webDriver);
    }

    private void assertBaseClusterItems(final List<String> expetedItems, final List<String> actualItems) {
        String expected = StringUtils.join(expetedItems, "\n");
        String current = StringUtils.join(actualItems, "\n");

        assertEquals(expected, current);
    }

    public BaseClusterPage edit(final String baseClusterName) {
        Optional<WebElement> clusterOptional = getClusterRows().stream().filter(r -> baseClusterName.equals(r.getText())).findFirst();
        assertTrue(clusterOptional.isPresent());
        waitForClick(clusterOptional.get());

        return new BaseClusterPage(webDriver);
    }

    private BaseClusterSearchPage assertPageIsOpen() {
        waitTextInElement(pageTitle, "Base Clusters");
        waitPageLoaderBeHide();
        return this;
    }

    private List<WebElement> getClusterRows() {
        return getElementsWhenTheyExists(By.className("editable-row"));
    }
}
