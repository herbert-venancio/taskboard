package objective.taskboard.it.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import objective.taskboard.it.AbstractUiFragment;
import objective.taskboard.it.components.guards.LeaveConfirmationGuard;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TabsRouterComponent extends AbstractComponent {

    public static final String TABS_ROUTER_TAG = "tb-tabs-router";

    public static final String TAB_ROUTER_TAG = "tb-tab-router";

    private Map<String, TabComponent<?>> tabs = new LinkedHashMap<>();

    public TabsRouterComponent(WebDriver driver, By by) {
        super(driver, by);
    }

    public <T extends AbstractUiFragment> void addTab(
            String tabName,
            String componentName,
            TabFactory<T> fragment) {
        tabs.put(tabName, new TabComponent<>(webDriver, this, tabName, componentName, fragment));
    }

    public <TO extends AbstractUiFragment> SelectTab<AbstractUiFragment, TO> selectTab(String tabName) {
        return selectTab(null, tabName);
    }

    @SuppressWarnings("unchecked")
    public <FROM extends AbstractUiFragment, TO extends AbstractUiFragment> SelectTab<FROM, TO> selectTab(String from, String to) {
        TabComponent<FROM> fromTab = (TabComponent<FROM>) tabs.get(from);
        TabComponent<TO> toTab = (TabComponent<TO>) tabs.get(to);
        return new SelectTab<>(this, fromTab, toTab);
    }

    public void select(String tabName) {
        WebElement tab = tabs.get(tabName).component();
        waitForClick(tab);
    }

    public void waitAllTabsExists() {
        tabs.values().forEach(TabComponent::waitUntilExists);
    }

    public interface TabFactory<T extends AbstractUiFragment> extends BiFunction<WebDriver, TabComponent<T>, T> { }

    public static class SelectTab<FROM extends AbstractUiFragment, TO extends AbstractUiFragment> {

        private final TabsRouterComponent tabs;
        private final TabComponent<FROM> from;
        private final TabComponent<TO> to;

        public SelectTab(TabsRouterComponent tabs, TabComponent<FROM> from, TabComponent<TO> to) {
            this.tabs = tabs;
            this.from = from;
            this.to = to;
        }

        public FROM expectConfirmationAndStay() {
            tabs.select(to.tabName);
            LeaveConfirmationGuard.stay(tabs.webDriver);
            return from.waitIsSelected();
        }

        public TO expectConfirmationAndLeave() {
            tabs.select(to.tabName);
            LeaveConfirmationGuard.leave(tabs.webDriver);
            return to.waitIsSelected();
        }

        public TO expectNoConfirmation() {
            tabs.select(to.tabName);
            LeaveConfirmationGuard.waitIsClosed(tabs.webDriver);
            return to.waitIsSelected();
        }

        public TO waitLoaded() {
            tabs.select(to.tabName);
            return to.waitIsSelected();
        }
    }
}
