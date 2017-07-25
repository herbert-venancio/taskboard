package objective.taskboard.google;

public class SpreadsheetUtils {
	private static String COLUMN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static int columnLetterToIndex(String column) {
        int columnNumber = 0;
        int length = column.length();
        
		if (column == null || length == 0)
            throw new IllegalArgumentException("Input is not valid!");

        for (int i = 0; i < length; i++) {
            char curr = column.charAt(i);
            columnNumber += (curr - 'A' + 1) * Math.pow(26, length - i - 1);
        }

        return columnNumber - 1;
    }

    public static String columnIndexToLetter(Integer index) {
        int base = COLUMN_CHARS.length();
        int column = index + 1;
        int temp = 0;
        StringBuilder result = new StringBuilder();
        
        while (column > 0) {
        	temp = (column - 1) % base;
        	result.insert(0, COLUMN_CHARS.charAt(temp));
        	column = (column - temp - 1) / base;
        }

        return result.toString();
    }
}
