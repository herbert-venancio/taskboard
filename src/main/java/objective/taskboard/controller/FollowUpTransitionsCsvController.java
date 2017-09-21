package objective.taskboard.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.followup.FollowupData;
import objective.taskboard.followup.FollowupDataProvider;
import objective.taskboard.followup.TransitionDataRow;
import objective.taskboard.followup.TransitionDataSet;
import objective.taskboard.utils.IOUtilities;
import objective.taskboard.utils.ZipUtils;
import objective.taskboard.utils.ZipUtils.ZipStreamEntry;

@Controller
public class FollowUpTransitionsCsvController {
    private static final Logger log = LoggerFactory.getLogger(FollowUpTransitionsCsvController.class);

    private static final CSVFormat CSV_FILE_FORMAT = CSVFormat.DEFAULT.withDelimiter(',').withRecordSeparator("\n");

    @Autowired
    private FollowUpFacade followUpFacade;

    @RequestMapping("/followup-transitions")
    public String page() {
        return "followup-transitions";
    }

    @RequestMapping(value = "/ws/followup/transitions", method = RequestMethod.POST)
    public ResponseEntity<Object> transitions(@RequestParam("projects") String projects, @RequestParam("timezone") String zoneId) {
        if (ObjectUtils.isEmpty(projects))
            return new ResponseEntity<>("You must provide a list of projects separated by comma", BAD_REQUEST);

        if (StringUtils.isEmpty(zoneId))
            return new ResponseEntity<>("You must provide a timezone id", BAD_REQUEST);

        String projectsToFileName = projects.replace(',','-');

        try {
            ZoneId timezone = ZoneId.of(zoneId);
            FollowupDataProvider provider = followUpFacade.getProvider(Optional.empty());
            FollowupData jiraData = provider.getJiraData(projects.split(","), timezone);

            Stream<ZipStreamEntry> stream = Stream.concat(
                    jiraData.analyticsTransitionsDsList.stream()
                        .map(ds -> getCSV(projectsToFileName + "_Analytic_" + ds.issueType, ds))
                    , jiraData.syntheticsTransitionsDsList.stream()
                        .map(ds -> getCSV(projectsToFileName + "_Synthetic_" + ds.issueType, ds))
            );

            return ResponseEntity.ok()
                    .contentType(new MediaType("application", "zip"))
                    .header("Content-Disposition", "attachment; filename=" + projectsToFileName + "_transitions.zip")
                    .body(IOUtilities.asResource(ZipUtils.zipToByteArray(stream)));

        } catch(DateTimeException e) {
            log.warn("Invalid timezone", e);
            return new ResponseEntity<>("Invalid timezone id: " + zoneId, BAD_REQUEST);
        } catch(Exception e){
            log.warn("Error generating transitions reports", e);
            return new ResponseEntity<>(e.getMessage() == null ? e.toString() : e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    private ZipStreamEntry getCSV(String fileName, TransitionDataSet<?> ds) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(stream);
        try {
            CSVPrinter csvFilePrinter = new CSVPrinter(osw, CSV_FILE_FORMAT);
            csvFilePrinter.printRecord(ds.headers);

            for (TransitionDataRow row : ds.rows)
                csvFilePrinter.printRecord(row.getAsStringList());

            csvFilePrinter.flush();
            csvFilePrinter.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return new ZipStreamEntry(new ByteArrayInputStream(stream.toByteArray()), new ZipEntry(fileName  + ".csv"));
    }
}
