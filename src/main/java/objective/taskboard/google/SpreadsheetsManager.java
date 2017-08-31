package objective.taskboard.google;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SpreadsheetsManager {
	
	private final Spreadsheets spreadsheets;
	
    public SpreadsheetsManager(Spreadsheets spreadsheets) {
		this.spreadsheets = spreadsheets;
	}

	public List<List<Object>> readRange(String spreadsheetId, String range) throws SpreadsheeNotFoundException {
	    try {
            ValueRange valueRange = spreadsheets.values().get(spreadsheetId, range).execute();
            return valueRange.getValues();

	    } catch (GoogleJsonResponseException ex) {
	        if (ex.getStatusCode() == 404)
                throw new SpreadsheeNotFoundException(spreadsheetId);
	        
	        throw new RuntimeException(ex);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
        try {
            spreadsheets.batchUpdate(spreadsheetId, content).execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Sheet getSheet(String spreadsheetId, String sheetTitle) {
        Spreadsheet spreadsheet;
        try {
            spreadsheet = spreadsheets.get(spreadsheetId).execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Optional<Sheet> sheet = spreadsheet.getSheets().stream()
                .filter(s -> s.getProperties().getTitle().equals(sheetTitle))
                .findFirst();

        if (!sheet.isPresent())
            throw new IllegalArgumentException("Cannot find a sheet with title '" + sheetTitle + "'");

        return sheet.get();
    }
    
    public static class SpreadsheeNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 4074725260811075034L;
        
        public SpreadsheeNotFoundException(String spreadsheetId) {
            super("Spreadsheet with id '" + spreadsheetId + "' not found");
        }
    }

}
