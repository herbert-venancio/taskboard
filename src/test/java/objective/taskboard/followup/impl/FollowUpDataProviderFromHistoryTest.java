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

package objective.taskboard.followup.impl;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempDirectory;
import static objective.taskboard.followup.FollowUpHelper.assertFollowUpDataDefault;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_JSON;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_ZIP;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.FILE_NAME_FORMAT;
import static objective.taskboard.utils.ZipUtils.zip;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.followup.FromJiraDataRow;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpDataProviderFromHistoryTest {

    private static final String PROJECT_TEST = "PROJECT TEST";
    private static final String PROJECT_TEST_2 = "PROJECT TEST 2";
    private static final String YESTERDAY = DateTime.now().minusDays(1).toString(FILE_NAME_FORMAT);

    private FollowUpDataProviderFromHistory subject;

    @Mock
    private DataBaseDirectory dataBaseDirectory;

    @Before
    public void before() throws IOException {
        Path pathHistoryTemp = createTempDirectory(getClass().getSimpleName());
        when(dataBaseDirectory.path(anyString())).thenReturn(pathHistoryTemp);
        subject = new FollowUpDataProviderFromHistory(YESTERDAY, dataBaseDirectory);
    }

    @Test
    public void whenHasDataHistory_ShouldReturnSomeData() throws IOException, URISyntaxException {
        createProjectZip(PROJECT_TEST);

        List<FromJiraDataRow> jiraData = subject.getJiraData(PROJECT_TEST.split(",")).fromJiraDs.rows;

        assertEquals("Jira data size", jiraData.size(), 1);
        assertFollowUpDataDefault(jiraData.get(0));
    }

    @Test
    public void whenDoesNotHaveDataHistory_ShouldThrowAnIllegalStateException() {
        try {
            subject.getJiraData(PROJECT_TEST.split(","));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertNotNull("IllegalStateException shouldn't be null", e);
        }
    }

    @Test
    public void whenHasInvalidDataHistory_ShouldThrowAnIllegalStateException() throws URISyntaxException {
        try {
            Path pathJSON = Paths.get(getClass().getResource("followUpDataHistoryExpected.json").toURI());
            Path zipFile = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST).resolve(YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP);
            zip(pathJSON, zipFile);

            subject.getJiraData(PROJECT_TEST.split(","));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertNotNull("IllegalStateException shouldn't be null", e);
        }
    }

    @Test
    public void whenTwoProjectsHasDataHistory_ShouldReturnSomeData() throws IOException,  URISyntaxException {
        createProjectZip(PROJECT_TEST);
        createProjectZip(PROJECT_TEST_2);

        String projects = PROJECT_TEST + "," + PROJECT_TEST_2;
        List<FromJiraDataRow> jiraData = subject.getJiraData(projects.split(",")).fromJiraDs.rows;

        assertEquals("Jira data size", jiraData.size(), 2);
        assertFollowUpDataDefault(jiraData.get(0));
        assertFollowUpDataDefault(jiraData.get(1));
    }

    @After
    public void after() {
        deleteQuietly(dataBaseDirectory.path(anyString()).toFile());
    }

    private Path createProjectZip(String project) throws IOException, URISyntaxException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(project);
        createDirectories(pathProject);

        Path pathInputJSON = Paths.get(getClass().getResource("followUpDataHistoryExpected.json").toURI());
        Path pathOutputJSON = pathProject.resolve(YESTERDAY + EXTENSION_JSON);
        copy(pathInputJSON, pathOutputJSON);

        Path zipFile = Paths.get(pathOutputJSON.toString() + EXTENSION_ZIP);
        zip(pathOutputJSON, zipFile);
        return pathProject;
    }

}
