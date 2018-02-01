package objective.taskboard.google;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.utils.ThreadUtils.sleepOrCry;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SpreadsheetsManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpreadsheetsManager.class);

    private final Spreadsheets spreadsheets;

    public SpreadsheetsManager(Spreadsheets spreadsheets) {
        this.spreadsheets = spreadsheets;
    }

    public List<List<Object>> readRange(String spreadsheetId, String range) throws SpreadsheeNotFoundException {
        ValueRange valueRange = handleException(spreadsheetId, () -> spreadsheets.values().get(spreadsheetId, range).execute());
        return valueRange.getValues();
    }

    public int getSheetId(String spreadsheetId, String sheetTitle) {
        Sheet sheet = getSheet(spreadsheetId, sheetTitle);
        return sheet.getProperties().getSheetId();
    }
    
    public String getLastColumnLetter(String spreadsheetId, String sheetTitle) {
        Sheet sheet = getSheet(spreadsheetId, sheetTitle);
        GridProperties gridProperties = sheet.getProperties().getGridProperties();
        Integer lastColumn = gridProperties.getColumnCount() - 1;

        return SpreadsheetUtils.columnIndexToLetter(lastColumn);
    }

    public void batchUpdate(String spreadsheetId, BatchUpdateSpreadsheetRequest content) {
        handleException(spreadsheetId, () -> spreadsheets.batchUpdate(spreadsheetId, content).execute());
    }

    public List<String> getSheetsTitles(String spreadsheetId) {
        Spreadsheet spreadsheet = getSpreadsheet(spreadsheetId);
        return spreadsheet.getSheets().stream().map(s -> s.getProperties().getTitle()).collect(toList());
    }

    private Sheet getSheet(String spreadsheetId, String sheetTitle) {
        Spreadsheet spreadsheet = getSpreadsheet(spreadsheetId);

        Optional<Sheet> sheet = spreadsheet.getSheets().stream()
                .filter(s -> s.getProperties().getTitle().equals(sheetTitle))
                .findFirst();

        if (!sheet.isPresent())
            throw new IllegalArgumentException("Cannot find a sheet with title '" + sheetTitle + "'");

        return sheet.get();
    }

    private Spreadsheet getSpreadsheet(String spreadsheetId) {
        return handleException(spreadsheetId, () -> spreadsheets.get(spreadsheetId).execute());
    }

    private <T> T handleException(String spreadsheetId, SpreadsheetsRequest<T> request) {
        try {
            return request.execute();
        } catch (GoogleJsonResponseException ex) {
            if (ex.getStatusCode() == 401)
                throw new SpreadsheeInvalidCredentialsException();
            else if (ex.getStatusCode() == 403)
                throw new SpreadsheeDailyLimitException();
            else if (ex.getStatusCode() == 404)
                throw new SpreadsheeNotFoundException(spreadsheetId);
            else if (ex.getStatusCode() >= 500)
                exponentialBackOff(() -> request.execute());

            throw new RuntimeException(ex);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T> T exponentialBackOff(SpreadsheetsRequest<T> request) {
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(3000L, 1.5);
        exponentialBackOff.setMaxElapsedTime(30000);
        BackOffExecution exec = exponentialBackOff.start();

        while(true) {
            try {
                return request.execute();
            } catch (IOException e) {
                long timeToWait = exec.nextBackOff();
                if (timeToWait == BackOffExecution.STOP)
                    throw new SpreadsheeBackendException();

                log.info("Execution failed, will retry after" + " " + timeToWait + " ms");
                sleepOrCry(timeToWait);
            }
        }
    }

    private interface SpreadsheetsRequest<T> {
        T execute() throws IOException;
    }

    public static class SpreadsheetException extends RuntimeException {
        private static final long serialVersionUID = 1656139289656324685L;

        public SpreadsheetException() {}

        public SpreadsheetException(String message) {
            super(message);
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class SpreadsheeNotFoundException extends SpreadsheetException {
        private static final long serialVersionUID = 4074725260811075034L;

        public SpreadsheeNotFoundException(String spreadsheetId) {
            super("Spreadsheet with id '" + spreadsheetId + "' not found.");
        }
    }

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason="We are having communication issues with Google Spreadsheet. Please, try again in 15 minutes.")
    public static class SpreadsheeBackendException extends SpreadsheetException {
        private static final long serialVersionUID = 6453134117524063963L;
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason="Daily limit exceeded. Please, try again tomorrow.")
    public static class SpreadsheeDailyLimitException extends SpreadsheetException {
        private static final long serialVersionUID = 6179253232804985065L;
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Invalid Credentials.")
    public static class SpreadsheeInvalidCredentialsException extends SpreadsheetException {
        private static final long serialVersionUID = -5332931549640852185L;
    }

}
