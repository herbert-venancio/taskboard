package objective.taskboard.it.basecluster;

import static objective.taskboard.it.components.guards.AccessGuard.assertCantAccess;

import org.junit.Test;

import objective.taskboard.it.AbstractUIWithCoverageIntegrationTest;
import objective.taskboard.it.MainPage;
import objective.taskboard.testUtils.LoginUtils;

public class BaseClusterIT extends AbstractUIWithCoverageIntegrationTest {

    @Test
    public void accessMenuShouldBeNotVisible_whenHasNotAdminCredentials() {
        MainPage mainPage = LoginUtils.doLoginAsDeveloper(webDriver);

        mainPage
            .openMenuFilters()
            .assertBaseClusterButtonNotVisible();

        assertCantAccess(webDriver, BaseClusterSearchPage.getPageUrl());
        assertCantAccess(webDriver, BaseClusterPage.getCreatePageUrl());
        assertCantAccess(webDriver, BaseClusterPage.getEditionPageUrl(1));
    }

    @Test
    public void accessBaseClusterSearchAnd_baseClusterListShouldHasItems() {
        MainPage mainPage = LoginUtils.doLoginAsAdmin(webDriver);

        mainPage
            .openMenuFilters()
            .assertBaseClusterButtonVisible()
            .openBaseClusterSearch()
            .assertBaseClusters(
                "Base Sizing Cluster",
                "Second Base Sizing Cluster",
                "Third Base Sizing Cluster"
            );
    }

    @Test
    public void editABaseClusterThen_changesShouldBeVisibleOnTheSearchPage() {
        MainPage mainPage = LoginUtils.doLoginAsAdmin(webDriver);

        String nameChaged = "Base Sizing Cluster CHANGED";

        BaseClusterPage baseCluster = mainPage
            .openMenuFilters()
            .assertBaseClusterButtonVisible()
            .openBaseClusterSearch()
            .edit("Base Sizing Cluster");

        BaseClusterSearchPage searchPage = baseCluster
            .assertSaveButtonDisabled()
                .setName(nameChaged)
                .setEffort("Alpha Bug", "XS", "10")
                .setCycle("Alpha Bug", "XS", "5")
                .setEffort("Alpha Test", "M", "7")
                .setCycle("Alpha Test", "M", "3")
            .save()
            .assertSuccessMessageIsOpen()
            .assertSaveButtonDisabled()
            .backToSearchPage();

        searchPage
            .assertBaseClusters(
                nameChaged,
                "Second Base Sizing Cluster",
                "Third Base Sizing Cluster"
            )
            .edit(nameChaged)
            .assertName(nameChaged)
            .assertEffort("Alpha Bug", "XS", "10")
            .assertCycle("Alpha Bug", "XS", "5")
            .assertEffort("Alpha Test", "M", "7")
            .assertCycle("Alpha Test", "M", "3");
    }

    @Test
    public void editAllBaseClustersThen_fieldsShouldBeValidated() {
        MainPage mainPage = LoginUtils.doLoginAsAdmin(webDriver);

        BaseClusterSearchPage searchPage = mainPage
            .openMenuFilters()
            .assertBaseClusterButtonVisible()
            .openBaseClusterSearch()
            .assertBaseClusters(
                "Base Sizing Cluster",
                "Second Base Sizing Cluster",
                "Third Base Sizing Cluster"
            );

        searchPage
            .edit("Base Sizing Cluster")
            .assertSaveButtonDisabled()
                .setName("")
                .setEffort("Alpha Bug", "XS", "")
                .setCycle("Alpha Bug", "XS", "")
                .setEffort("Alpha Test", "M", "-1")
                .setCycle("Alpha Test", "M", "-500")
            .save()
            .assertErrorMessageIsOpen()
                .setName("Base Sizing Cluster CHANGED")
                .setEffort("Alpha Bug", "XS", "10")
                .setCycle("Alpha Bug", "XS", "5")
                .setEffort("Alpha Test", "M", "7")
                .setCycle("Alpha Test", "M", "3")
            .save()
            .assertSuccessMessageIsOpen()
            .assertSaveButtonDisabled()
            .refreshPage()
            .assertName("Base Sizing Cluster CHANGED")
            .assertEffort("Alpha Bug", "XS", "10")
            .assertCycle("Alpha Bug", "XS", "5")
            .assertEffort("Alpha Test", "M", "7")
            .assertCycle("Alpha Test", "M", "3")
                .backToSearchPage();

        searchPage
            .edit("Second Base Sizing Cluster")
            .assertSaveButtonDisabled()
                .setName("   ")
            .save()
            .assertErrorMessageIsOpen()
                .setName("Second Base Sizing Cluster CHANGED TOO")
            .save()
            .assertSuccessMessageIsOpen()
            .assertSaveButtonDisabled()
                .backToSearchPage();

        searchPage
            .edit("Third Base Sizing Cluster")
            .assertSaveButtonDisabled()
                .setName("")
                .setEffort("Alpha Bug", "XS", "")
                .setCycle("Alpha Bug", "XS", "-1")
            .save()
            .assertErrorMessageIsOpen()
                .backToSearchPageConfirmingLoseChanges();

        searchPage.assertBaseClusters(
            "Base Sizing Cluster CHANGED",
            "Second Base Sizing Cluster CHANGED TOO",
            "Third Base Sizing Cluster"
        );
    }

    @Test
    public void createANewBaseCluster() {
        MainPage mainPage = LoginUtils.doLoginAsAdmin(webDriver);

        BaseClusterSearchPage searchPage = mainPage
            .openMenuFilters()
            .assertBaseClusterButtonVisible()
            .openBaseClusterSearch()
            .assertBaseClusters(
                "Base Sizing Cluster",
                "Second Base Sizing Cluster",
                "Third Base Sizing Cluster"
            );

        searchPage.newBaseCluster()
            .assertSaveButtonDisabled()
                .setName("New base cluster")
                .setEffort("Alpha Bug", "XS", "10")
                .setCycle("Alpha Bug", "XS", "5")
                .setEffort("Alpha Test", "M", "7")
                .setCycle("Alpha Test", "M", "3")
            .save()
            .assertName("New base cluster")
            .assertEffort("Alpha Bug", "XS", "10")
            .assertCycle("Alpha Bug", "XS", "5")
            .assertEffort("Alpha Test", "M", "7")
            .assertCycle("Alpha Test", "M", "3")
                .backToSearchPage()
            .assertBaseClusters(
                "Base Sizing Cluster",
                "Second Base Sizing Cluster",
                "Third Base Sizing Cluster",
                "New base cluster"
            );
    }
}
