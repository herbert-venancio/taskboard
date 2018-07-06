/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */
package objective.taskboard.it;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class ReleaseFilterIT extends AuthenticatedIntegrationTest {

    private MainPage mainPage; 
    
    @Before 
    public void beforeTest() { 
        mainPage = MainPage.produce(webDriver); 
    } 
     
    @After
    public void afterTest() { 
        mainPage = null; 
    }

    @Test
    public void whenNoProjectsIsSelected_noReleasesLabelShowup() {
        MenuFilters menuFilters = mainPage.assertLabelRelease("Release")
                .openMenuFilters();
        menuFilters.openCardFieldFilters()
                .clickCheckAllFilter("Project");
        mainPage.assertLabelRelease("No releases for visible projects");
        menuFilters.clickCheckAllFilter("Project");
        mainPage.assertLabelRelease("Release");
    }

    @Test
    public void whenFilterByRelease_onlyIssueInTheReleaseShowUp() {
        mainPage
                .filterByRelease("TASKB - 1.0")
                .assertVisibleIssues("TASKB-186", "TASKB-238", "TASKB-572");
    }

    @Test
    public void whenWebhookProjectVersionUpdate_updateReleaseOfIssues() {
        mainPage.assertUpdatedIssues();

        emulateVersionUpdate("12550", "1.0-edited");

        String[] updatedIssues = {"TASKB-186", "TASKB-238", "TASKB-572"};

        mainPage
                .assertUpdatedIssues(updatedIssues)
                .waitReleaseFilterContains("TASKB - 1.0-edited")
                .filterByRelease("TASKB - 1.0-edited")
                .assertVisibleIssues(updatedIssues);

        emulateVersionUpdate("12550", "1.0-changed");
        mainPage
                .assertUpdatedIssues(updatedIssues)
                .assertSelectedRelease("TASKB - 1.0-changed")
                .assertVisibleIssues(updatedIssues);
    }

    @Test
    public void whenWebhookChangeReleaseOfIssue_thenFilterByReleaseContinueWorking() {
        mainPage.assertUpdatedIssues();

        emulateUpdateIssue("TASKB-606", "{\"customfield_11455\":{\"id\": \"12552\",\"name\": \"3.0\"}}");

        String[] updatedIssues = {"TASKB-606", "TASKB-235", "TASKB-601"};

        mainPage
            .assertUpdatedIssues(updatedIssues)
            .filterByRelease("TASKB - 3.0")
            .assertVisibleIssues(updatedIssues);

        emulateUpdateIssue("TASKB-606", "{\"customfield_11455\":{\"id\": \"12551\",\"name\": \"2.0\"}}");
        
        mainPage
            .assertUpdatedIssues(updatedIssues)
            .filterByRelease("TASKB - 2.0")
            .assertVisibleIssues(updatedIssues);
    }

    @Test
    public void whenOpenReleaseDropDown_showSortedReleases() {
        Stream<WebElement> allReleases = mainPage.getAllReleases();
        List<String> releaseNameResult = allReleases.map(WebElement::getText).collect(Collectors.toList());
        List<String> releaseNameSortExpected = releaseNameResult.stream().collect(Collectors.toList());

        Collections.sort(releaseNameSortExpected);

        for(int i = 0 ; i < releaseNameResult.size(); i++) {
            assertEquals("Releases have to be sorted ", releaseNameSortExpected.get(i), releaseNameResult.get(i));
        }
    }
}
