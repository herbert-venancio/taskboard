package objective.taskboard.followup;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.utils.IOUtilities.ENCODE_UTF_8;
import static objective.taskboard.utils.IOUtilities.asResource;
import static objective.taskboard.utils.IOUtilities.write;
import static objective.taskboard.utils.ZipUtils.unzip;
import static objective.taskboard.utils.ZipUtils.zip;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.utils.DateTimeUtils;

@Component
public class FollowUpDataHistoryRepository {
    public static final String PATH_FOLLOWUP_HISTORY = "followup-history";
    public static final String FILE_NAME_FORMAT = "yyyyMMdd";
    public static final String EXTENSION_JSON = ".json";
    public static final String EXTENSION_ZIP = ".zip";

    private final DataBaseDirectory dataBaseDirectory;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new DateTimeUtils.ZonedDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    
    @Autowired
    public FollowUpDataHistoryRepository(DataBaseDirectory dataBaseDirectory) {
        this.dataBaseDirectory = dataBaseDirectory;
    }

    public void save(String projectKey, FollowupData data) throws IOException {
        Path pathProject = dataBaseDirectory.path(PATH_FOLLOWUP_HISTORY).resolve(projectKey);
        if (!pathProject.toFile().exists())
            createDirectories(pathProject);

        String today = DateTime.now().toString(FILE_NAME_FORMAT);
        Path pathJSON = pathProject.resolve(today + EXTENSION_JSON);

        try {
            String json = gson.toJson(sanitizeData(data));
			write(pathJSON.toFile(), json);

            Path pathZIP = Paths.get(pathJSON.toString() + EXTENSION_ZIP);
            deleteQuietly(pathZIP.toFile());
            zip(pathJSON, pathZIP);
        } finally {
            deleteQuietly(pathJSON.toFile());
        }
    }

    private FollowupData sanitizeData(FollowupData data) {
        return new FollowupData(
                nullIfEmpty(data.fromJiraDs), 
                nullIfEmpty(data.analyticsTransitionsDsList),
                headerOnly(data.syntheticsTransitionsDsList));
    }

    public FollowupData get(String date, ZoneId timezone, String... projectsKey) {
        FollowUpDataLoader loader = new FollowUpDataLoader(gson, timezone);

        for (String project : projectsKey) {
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
    
    public List<String> getHistoryGivenProjects(String... projectsKey) {
        if (projectsKey.length == 0)
            return Collections.emptyList();

        Set<String> history = null;
        
        for (String projectKey : projectsKey) {
            Set<String> projectHistory = getHistoryByProject(projectKey);

            if (history == null) {
                history = new HashSet<>(projectHistory);
            } else {
                history.retainAll(projectHistory);
            }
            
            if (history.isEmpty())
                return Collections.emptyList();
        }
        
        return history.stream().sorted().collect(toList());
    }

    private Set<String> getHistoryByProject(String projectKey) {
        Set<String> history = new HashSet<String>();

        Path pathProject = dataBaseDirectory.path(PATH_FOLLOWUP_HISTORY).resolve(projectKey);
        if (!pathProject.toFile().exists())
            return history;

        try {
            Iterable<Path> paths = walk(pathProject)::iterator;
            for (Path path : paths) {
                if (path.toFile().isDirectory())
                    continue;

                String fileName = path.toFile().getName().replace(EXTENSION_JSON + EXTENSION_ZIP, "");
                String today = DateTime.now().toString(FILE_NAME_FORMAT);
                if (today.equals(fileName))
                    continue;

                history.add(fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return history;
    }

    private static FromJiraDataSet nullIfEmpty(FromJiraDataSet fromJiraDs) {
        if(isEmpty(fromJiraDs.rows)) {
            return null;
        }
        return new FromJiraDataSet(null, fromJiraDs.rows);
    }

    private static <T> List<T> nullIfEmpty(List<T> list) {
        return isEmpty(list) ? null : list;
    }

    /**
     * Should not persist data, only headers
     * @param syntheticsDsList
     * @return
     */
    private static List<SyntheticTransitionsDataSet> headerOnly(List<SyntheticTransitionsDataSet> syntheticsDsList) {
        if(isEmpty(syntheticsDsList))
            return null;
        return syntheticsDsList.stream()
                .map(s -> new SyntheticTransitionsDataSet(s.issueType, s.headers, null))
                .collect(Collectors.toList());
    }
}
