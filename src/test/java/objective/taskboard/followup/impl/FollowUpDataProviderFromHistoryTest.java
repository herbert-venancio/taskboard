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
import static objective.taskboard.followup.FollowUpHelper.assertFollowUpDataDefault;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_JSON;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_ZIP;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.FILE_NAME_FORMAT;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.PATH_FOLLOWUP_HISTORY;
import static objective.taskboard.utils.ZipUtils.zip;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.FollowUpData;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpDataProviderFromHistoryTest {

    private static final String PROJECT_TEST = "PROJECT TEST";
    private static final String TODAY = DateTime.now().toString(FILE_NAME_FORMAT);

    private FollowUpDataProviderFromHistory subject;

    @Before
    public void before() {
        subject = new FollowUpDataProviderFromHistory(TODAY);
    }

    @Test
    public void whenHasDataHistory_ShouldReturnSomeData() throws IOException, InterruptedException, URISyntaxException {
        Path pathProject = null;
        try {
            pathProject = Paths.get(PATH_FOLLOWUP_HISTORY, PROJECT_TEST);
            createDirectories(pathProject);

            Path pathInputJSON = Paths.get(getClass().getResource("followUpDataHistoryExpected.json").toURI());
            Path pathOutputJSON = pathProject.resolve(TODAY + EXTENSION_JSON);
            copy(pathInputJSON, pathOutputJSON);

            Path zipFile = Paths.get(pathOutputJSON.toString() + EXTENSION_ZIP);
            zip(pathOutputJSON, zipFile);

            List<FollowUpData> jiraData = subject.getJiraData(PROJECT_TEST.split(","));

            assertEquals("Jira data size", jiraData.size(), 1);
            assertFollowUpDataDefault(jiraData.get(0));
        } finally {
            if (pathProject != null)
                deleteQuietly(pathProject.toFile());
        }
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
    public void whenHasInvalidDataHistory_ShouldThrowAnIllegalStateException() throws IOException, URISyntaxException {
        Path zipFile = null;
        try {
            Path pathJSON = Paths.get(getClass().getResource("followUpDataHistoryExpected.json").toURI());
            zipFile = Paths.get(PATH_FOLLOWUP_HISTORY, PROJECT_TEST, TODAY + EXTENSION_JSON + EXTENSION_ZIP);
            zip(pathJSON, zipFile);

            subject.getJiraData(PROJECT_TEST.split(","));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertNotNull("IllegalStateException shouldn't be null", e);
        } finally {
            if (zipFile != null)
                deleteQuietly(zipFile.getParent().toFile());
        }
    }

}
