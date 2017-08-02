package objective.taskboard.google;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
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

    private int getColumnCount(String spreadsheetId) {
        try {
            Spreadsheet execute = spreadsheets.get(spreadsheetId).execute();
            Sheet sheet = execute.getSheets().get(0);
            SheetProperties properties = sheet.getProperties();
            GridProperties gridProperties = properties.getGridProperties();
            return gridProperties.getColumnCount();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public int getSheetId(String spreadsheetId, int sheetIndex) {
        try {
            List<Sheet> sheets = spreadsheets.get(spreadsheetId).execute().getSheets();
            Sheet sheet = sheets.get(sheetIndex);
            return sheet.getProperties().getSheetId();
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public String getLastColumnLetter(String spreadsheetId) {
        Integer lastColumn = getColumnCount(spreadsheetId);
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
