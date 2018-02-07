package objective.taskboard.followup;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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
    public static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern(FILE_NAME_FORMAT);
    public static final String EXTENSION_JSON = ".json";
    public static final String EXTENSION_ZIP = ".zip";

    private final DataBaseDirectory dataBaseDirectory;
    private FollowupClusterProvider clusterProvider;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new DateTimeUtils.ZonedDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    
    @Autowired
    public FollowUpDataHistoryRepository(DataBaseDirectory dataBaseDirectory, FollowupClusterProvider clusterProvider) {
        this.dataBaseDirectory = dataBaseDirectory;
        this.clusterProvider = clusterProvider;
    }

    public void save(String projectKey, LocalDate date, FollowupData data) throws IOException {
        Path pathProject = dataBaseDirectory.path(PATH_FOLLOWUP_HISTORY).resolve(projectKey);
        if (!pathProject.toFile().exists())
            createDirectories(pathProject);

        String dateString = date.format(FILE_NAME_FORMATTER);
        Path pathJSON = pathProject.resolve(dateString + EXTENSION_JSON);

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

    public FollowUpDataSnapshot get(String date, ZoneId timezone, String... projectsKey) {
        return load(date, timezone, asList(projectsKey));
    }

    private FollowUpDataSnapshot load(String date, ZoneId timezone, List<String> projectsKey) {
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

        return new FollowUpDataSnapshot(LocalDate.parse(date, FILE_NAME_FORMATTER), loader.create());
    }
    
    public List<String> getHistoryGivenProjects(String... projectsKey) {
        return getHistoryGivenProjects(asList(projectsKey));
    }
    
    public List<String> getHistoryGivenProjects(List<String> projectsKey) {
        if (projectsKey.isEmpty())
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
        Path pathProject = dataBaseDirectory.path(PATH_FOLLOWUP_HISTORY).resolve(projectKey);
        if (!pathProject.toFile().exists())
            return Collections.emptySet();

        String fileExtension = EXTENSION_JSON + EXTENSION_ZIP;
        String today = DateTime.now().toString(FILE_NAME_FORMAT);
        
        try {
            return Files.walk(pathProject)
                    .map(Path::toFile)
                    .filter(file -> file.isFile())
                    .filter(file -> file.getName().toLowerCase().endsWith(fileExtension))
                    .map(file -> file.getName().replace(fileExtension, ""))
                    .filter(fileName -> !today.equals(fileName))
                    .collect(toSet());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Iterate from oldest entry through <code>endDate</code> (exclusive).
     * 
     */
    public void forEachSnapshot(
            List<String> projectsKey, 
            String endDate, ZoneId timezone,
            Consumer<FollowUpDataSnapshot> action) {

        getHistoryGivenProjects(projectsKey).stream()
                .filter(d -> endDate == null ? true : d.compareTo(endDate) < 0)
                .map(d -> load(d, timezone, projectsKey))
                .forEach(action);
    }

    /**
     * Iterate from oldest entry through generated today (exclusive).
     * 
     */
    public void forEachSnapshot(
            List<String> projectsKey, 
            ZoneId timezone,
            Consumer<FollowUpDataSnapshot> action) {

        forEachSnapshot(projectsKey, null, timezone, action);
    }

    public FollowupCluster getClusterForProject(String projectKey) {
        return clusterProvider.getForProject(projectKey);
    }
}
