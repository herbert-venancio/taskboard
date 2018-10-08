package objective.taskboard.it.components;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import objective.taskboard.it.components.guards.LeaveConfirmationGuard;

public class TabsRouterComponent extends AbstractComponent {

    public static final String TABS_ROUTER_TAG = "tb-tabs-router";

    private static final String TAB_ROUTER_TAG = "tb-tab-router";

    public TabsRouterComponent(WebDriver driver, By by, String... tabsNames) {
        super(driver, by);
        waitAllTabsExists(tabsNames);
    }

    public void select(String tabName, String componentTag, Boolean leaveConfirmation) {
        WebElement tab = tabButtonEl(tabName);
        waitForClick(tab);

        if (leaveConfirmation)
            LeaveConfirmationGuard.leave(webDriver);
        else
            LeaveConfirmationGuard.waitIsClosed(webDriver);

        waitIsSelected(tab, componentTag);
    }

    public void selectButStay(String tabName) {
        WebElement tab = tabButtonEl(tabName);
        waitForClick(tab);
        LeaveConfirmationGuard.stay(webDriver);
    }

    private void waitAllTabsExists(String... tabsNames) {
        WebElement component = component();
        Stream.of(tabsNames)
            .forEach(tabName -> {
                waitUntilChildElementExists(component, cssSelector(TAB_ROUTER_TAG +"[name=\""+ tabName +"\"]"));
            });
    }

    private void waitIsSelected(WebElement tab, String componentTag) {
        waitTabHasActiveClass(tab);
        waitContentUpdated(componentTag);
    }

    private void waitTabHasActiveClass(WebElement tab) {
        waitAttributeValueInElementContains(tab, "class", "active");
    }

    private void waitContentUpdated(String componentTag) {
        WebElement tabContent = getChildElementWhenExists(component(), cssSelector(".content"));
        waitUntilChildElementExists(tabContent, tagName(componentTag));
    }

    private WebElement tabButtonEl(String tabName) {
        return component().findElement(cssSelector(TAB_ROUTER_TAG +"[name=\""+ tabName +"\"]"));
    }

}
