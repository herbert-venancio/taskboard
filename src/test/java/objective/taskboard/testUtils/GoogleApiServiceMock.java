package objective.taskboard.testUtils;

import static java.util.Arrays.asList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SIZING_METADATA;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.google.SpreadsheetsManager;

@Component
public class GoogleApiServiceMock implements GoogleApiService {

    @Override
    public boolean verifyAuthorization() throws IOException {   
        return true;
    }

    @Override
    public SpreadsheetsManager buildSpreadsheetsManager() {
        
        SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
        
        mockSheetRows(spreadsheetsManager, SHEET_SCOPE);
        mockSheetRows(spreadsheetsManager, SHEET_COST);
        mockSheetRows(spreadsheetsManager, SHEET_SIZING_METADATA);
        
        when(spreadsheetsManager.getSheetId(anyString(), anyString())).thenReturn(1);
        
        doNothing().when(spreadsheetsManager).batchUpdate(anyString(), anyObject());
        
        when(spreadsheetsManager.getSheetsTitles(anyString())).thenAnswer(invocation -> {
            String spreadsheetId = invocation.getArgumentAt(0, String.class);
            
            if ("spreadsheet-without-scope-sheet".equals(spreadsheetId))
                return asList("Timeline", SHEET_COST, SHEET_SIZING_METADATA);

            if ("spreadsheet-without-cost-sheet".equals(spreadsheetId))
                return asList(SHEET_SCOPE, "Timeline", SHEET_SIZING_METADATA);

            return asList(SHEET_SCOPE, "Timeline", SHEET_COST, SHEET_SIZING_METADATA);
        });
        
        return spreadsheetsManager;
    }

    private void mockSheetRows(SpreadsheetsManager spreadsheetsManager, String sheet) {
        List<List<Object>> rows = getSizingTemplateRows(sheet);
        String lastColumnLetter = SpreadsheetUtils.columnIndexToLetter(rows.get(0).size() - 1);

        when(spreadsheetsManager.readRange(anyString(), eq("'" + sheet + "'"))).thenReturn(rows);
        when(spreadsheetsManager.getLastColumnLetter(anyString(), eq(sheet))).thenReturn(lastColumnLetter);
    }

    @Override
    public void createAndStoreCredential(String authorizationCode) {
    }

    @Override
    public Optional<GoogleCredential> getCredential() {
        return null;
    }

    @Override
    public void removeCredential() {
    }
    
    private List<List<Object>> getSizingTemplateRows(String sheet) {
        List<List<Object>> rows = new ArrayList<>();
        try {
            Reader in = new FileReader("src/test/resources/objective-jira-teste/sizing-template/" + sheet + ".csv");
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
            for (CSVRecord record : records) {
                ArrayList<Object> row = new ArrayList<>();
                record.forEach(cell -> {
                    row.add(cell);
                });
                rows.add(row);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }


}
