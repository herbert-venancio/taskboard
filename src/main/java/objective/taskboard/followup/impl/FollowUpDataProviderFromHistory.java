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

import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.asList;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_JSON;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_ZIP;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.PATH_FOLLOWUP_HISTORY;
import static objective.taskboard.issueBuffer.IssueBufferState.ready;
import static objective.taskboard.utils.IOUtilities.ENCODE_UTF_8;
import static objective.taskboard.utils.IOUtilities.asResource;
import static objective.taskboard.utils.ZipUtils.unzip;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowupDataProvider;
import objective.taskboard.issueBuffer.IssueBufferState;

public class FollowUpDataProviderFromHistory implements FollowupDataProvider {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String date;

    public FollowUpDataProviderFromHistory(String date) {
        this.date = date;
    }

    @SuppressWarnings("serial")
    @Override
    public List<FollowUpData> getJiraData(String[] includeProjects) {
        List<String> projects = asList(includeProjects);

        List<FollowUpData> data = new ArrayList<FollowUpData>();
        for (String project : projects) {
            File fileZip = Paths.get(PATH_FOLLOWUP_HISTORY, project, date + EXTENSION_JSON + EXTENSION_ZIP).toFile();
            if (!fileZip.exists())
                throw new IllegalStateException(fileZip.toString() + " not found");

            Path temp = null;
            try {
                temp = createTempDirectory("FollowUpDataByDate");
                unzip(fileZip, temp);
                File fileJSON = temp.resolve(date + EXTENSION_JSON).toFile();
                if (!fileJSON.exists())
                    throw new IllegalStateException(fileJSON.toString() + " not found");
                String json = IOUtils.toString(asResource(fileJSON).getInputStream(), ENCODE_UTF_8);
                Type type = new TypeToken<List<FollowUpData>>(){}.getType();
                data.addAll(gson.fromJson(json, type));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (temp != null)
                    deleteQuietly(temp.toFile());
            }
        }
        return data;
    }

    @Override
    public IssueBufferState getFollowupState() {
        return ready;
    }

}
