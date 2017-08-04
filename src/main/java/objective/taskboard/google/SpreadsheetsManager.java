package objective.taskboard.google;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Sheet;
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

    private int getColumnCount(String spreadsheetId, int sheetIndex) {
        try {
            Sheet sheet = getSheet(spreadsheetId, sheetIndex);
            GridProperties gridProperties = sheet.getProperties().getGridProperties();
            return gridProperties.getColumnCount();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public int getSheetId(String spreadsheetId, int sheetIndex) {
        try {
            Sheet sheet = getSheet(spreadsheetId, sheetIndex);
            return sheet.getProperties().getSheetId();
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Sheet getSheet(String spreadsheetId, int sheetIndex) throws IOException {
        List<Sheet> sheets = spreadsheets.get(spreadsheetId).execute().getSheets();
        List<Sheet> visibleSheets = sheets.stream()
                .filter(s -> s.getProperties().getHidden() == null || !s.getProperties().getHidden())
                .collect(toList());

        return visibleSheets.get(sheetIndex);
    }
    
    public String getLastColumnLetter(String spreadsheetId, int sheetIndex) {
        Integer lastColumn = getColumnCount(spreadsheetId, sheetIndex);
        return SpreadsheetUtils.columnIndexToLetter(lastColumn - 1);
    }
    
    public void batchUpdate(String spreadsheetId, BatchUpdateSpreadsheetRequest content) {
        try {
            spreadsheets.batchUpdate(spreadsheetId, content).execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static class SpreadsheeNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 4074725260811075034L;
        
        public SpreadsheeNotFoundException(String spreadsheetId) {
            super("Spreadsheet with id '" + spreadsheetId + "' not found");
        }
    }

}
