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
import static objective.taskboard.followup.impl.FollowUpTransitionsDataProvider.HEADER_ISSUE_TYPE_COLUMN_NAME;
import static objective.taskboard.issueBuffer.IssueBufferState.ready;
import static objective.taskboard.utils.IOUtilities.ENCODE_UTF_8;
import static objective.taskboard.utils.IOUtilities.asResource;
import static objective.taskboard.utils.ZipUtils.unzip;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import objective.taskboard.Constants;
import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowupData;
import objective.taskboard.followup.FollowupDataProvider;
import objective.taskboard.followup.FromJiraDataRow;
import objective.taskboard.followup.FromJiraDataSet;
import objective.taskboard.followup.SyntheticTransitionsDataSet;
import objective.taskboard.issueBuffer.IssueBufferState;
import objective.taskboard.utils.DateTimeUtils;

public class FollowUpDataProviderFromHistory implements FollowupDataProvider {

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new DateTimeUtils.ZonedDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    private final String date;
    private final DataBaseDirectory dataBaseDirectory;

    public FollowUpDataProviderFromHistory(String date, DataBaseDirectory dataBaseDirectory) {
        this.date = date;
        this.dataBaseDirectory = dataBaseDirectory;
    }

    @Override
    public FollowupData getJiraData(String[] includeProjects, ZoneId timezone) {
        List<String> projects = asList(includeProjects);

        V2_Loader loader = new V2_Loader(timezone);
        for (String project : projects) {
            String fileZipName = date + EXTENSION_JSON + EXTENSION_ZIP;
            File fileZip = dataBaseDirectory.path(PATH_FOLLOWUP_HISTORY).resolve(project).resolve(fileZipName).toFile();
            if (!fileZip.exists())
                throw new IllegalStateException(fileZip.toString() + " not found");

            Path temp = null;
            try {
                temp = createTempDirectory(getClass().getSimpleName());
                unzip(fileZip, temp);

                File fileJSON = temp.resolve(date + EXTENSION_JSON).toFile();
                if (!fileJSON.exists())
                    throw new IllegalStateException(fileJSON.toString() + " not found");

                String json = IOUtils.toString(asResource(fileJSON).getInputStream(), ENCODE_UTF_8);
                JsonElement jsonElement = new JsonParser().parse(json);

                loader.load(jsonElement);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            } finally {
                if (temp != null)
                    deleteQuietly(temp.toFile());
            }
        }
        return loader.create();
    }

    private class V2_Loader extends BaseLoader {

        public V2_Loader(ZoneId timezone) {
            super(timezone);
        }

        public V2_Loader load(JsonElement element) {
            if(matchVersion(element, FollowupData.Version.VERSION_2)) {
                doLoad(gson.fromJson(element, FollowupData.class));
            } else {
                doLoad(new V1_Loader(timezone).load(element).upgrade());
            }
            return this;
        }

    }

    private class V1_Loader extends BaseLoader {

        public V1_Loader(ZoneId timezone) {
            super(timezone);
        }

        public V1_Loader load(JsonElement element) {
            if(matchVersion(element, FollowupData.Version.VERSION_1)) {
                doLoad(gson.fromJson(element, FollowupData.class));
            } else {
                doLoad(new V0_Loader().load(element).upgrade());
            }
            return this;
        }

        private FollowupData upgrade() {
            for(String issueType : syntheticsHeaders.keySet()) {
                syntheticsHeaders.get(issueType).add(1, HEADER_ISSUE_TYPE_COLUMN_NAME);
            }
            return create();
        }
    }

    private abstract class BaseLoader {

        protected final ZoneId timezone;
        protected List<FromJiraDataRow> fromJira = new ArrayList<>();
        protected Set<String> types = new LinkedHashSet<>();
        protected ListMultimap<String, String> analyticsHeaders = LinkedListMultimap.create();
        protected ListMultimap<String, AnalyticsTransitionsDataRow> analytics = LinkedListMultimap.create();
        protected ListMultimap<String, String> syntheticsHeaders = LinkedListMultimap.create();

        protected BaseLoader(ZoneId timezone) {
            this.timezone = timezone;
        }

        protected void doLoad(FollowupData data) {
            fromJira.addAll(data.fromJiraDs.rows);
            if(data.analyticsTransitionsDsList != null) {
                for (AnalyticsTransitionsDataSet analyticsDs : data.analyticsTransitionsDsList) {
                    final String type = analyticsDs.issueType;
                    types.add(type);
                    addHeaders(analyticsHeaders, type, analyticsDs.headers);
                    analyticsDs.rows.stream()
                            .map(this::convertDates)
                            .forEach(row -> analytics.put(type, row));
                }
            }
            if(data.syntheticsTransitionsDsList != null) {
                for (SyntheticTransitionsDataSet syntheticsDs : data.syntheticsTransitionsDsList) {
                    String type = syntheticsDs.issueType;
                    addHeaders(syntheticsHeaders, type, syntheticsDs.headers);
                }
            }
        }

        /**
         * Adds unique headers at most likely index.
         * E.g.:
         *   headerMap contains: Done, QA, Doing, To Do
         *   newHeaders contains: Done, Doing, To Do, Open
         *
         *   expected result: Done, QA, Doing, To Do, Open
         * @param headerMap where headers will be aggregated by type
         * @param type issue type to be used as key in headerMap
         * @param newHeaders new headers to be added
         */
        protected void addHeaders(ListMultimap<String, String> headerMap, String type, List<String> newHeaders) {
            int insertIndex = 0;
            for(String header : newHeaders) {
                if(headerMap.containsEntry(type, header)) {
                    insertIndex = headerMap.get(type).indexOf(header) + 1;
                } else {
                    headerMap.get(type).add(insertIndex++, header);
                }
            }
        }

        private AnalyticsTransitionsDataRow convertDates(AnalyticsTransitionsDataRow row) {
            List<ZonedDateTime> dates = row.transitionsDates.stream()
                    .map(date -> date != null ? date.withZoneSameInstant(timezone) : null)
                    .collect(Collectors.toList());
            return new AnalyticsTransitionsDataRow(row.issueKey, row.issueType, dates);
        }

        public FollowupData create() {
            FromJiraDataSet fromJiraDs = new FromJiraDataSet(Constants.FROMJIRA_HEADERS, fromJira);
            List<AnalyticsTransitionsDataSet> analyticsDsList = new LinkedList<>();
            for(String type : types) {
                List<String> headers = new LinkedList<>(analyticsHeaders.get(type));
                List<AnalyticsTransitionsDataRow> rows = analytics.get(type);
                analyticsDsList.add(new AnalyticsTransitionsDataSet(type, headers, rows));
            }
            List<SyntheticTransitionsDataSet> syntheticDsList = new LinkedList<>();
            for (AnalyticsTransitionsDataSet dataset : analyticsDsList) {
                String type = dataset.issueType;
                List<String> headers = new LinkedList<>(syntheticsHeaders.get(type));
                String[] statuses = dataset.headers.subList(2, dataset.headers.size()).toArray(new String[0]);
                List<String> doneStatuses = headers.stream()
                        .filter(h -> h.contains("/"))
                        .flatMap(h -> asList(h.split("/")).stream())
                        .collect(Collectors.toList());
                syntheticDsList.add(FollowUpTransitionsDataProvider.getSyntheticTransitionsDs(headers, statuses, doneStatuses, dataset));
            }
            return new FollowupData(fromJiraDs, analyticsDsList, syntheticDsList);
        }
    }

    private class V0_Loader {

        private List<FromJiraDataRow> data = new ArrayList<>();

        @SuppressWarnings("serial")
        private V0_Loader load(JsonElement element) {
            Type type = new TypeToken<List<FromJiraDataRow>>(){}.getType();
            data.addAll(gson.fromJson(element, type));
            return this;
        }

        private FollowupData upgrade() {
            return new FollowupData(new FromJiraDataSet(Constants.FROMJIRA_HEADERS, data), null, null);
        }
    }

    private static boolean matchVersion(JsonElement element, FollowupData.Version expectedVersion) {
        if(!element.isJsonObject())
            return false;
        JsonObject obj = element.getAsJsonObject();
        if(!obj.has("followupDataVersion"))
            return false;
        JsonElement version = obj.get("followupDataVersion");
        if(!version.isJsonPrimitive() || !version.getAsJsonPrimitive().isString())
            return false;
        if(!Objects.equals(expectedVersion.value, version.getAsString()))
            return false;
        return true;
    }

    @Override
    public IssueBufferState getFollowupState() {
        return ready;
    }
}
