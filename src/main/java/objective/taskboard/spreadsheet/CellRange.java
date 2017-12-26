package objective.taskboard.spreadsheet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static objective.taskboard.google.SpreadsheetUtils.columnIndexToLetter;
import static objective.taskboard.google.SpreadsheetUtils.columnLetterToIndex;

public class CellRange {

    public static final String MIN_COLUMN_NAME = "A";
    public static final String MAX_COLUMN_NAME = "XFD";
    public static final int MIN_COLUMN_INDEX = columnLetterToIndex(MIN_COLUMN_NAME);
    public static final int MAX_COLUMN_INDEX = columnLetterToIndex(MAX_COLUMN_NAME);
    public static final int MIN_ROW_INDEX = 0;
    public static final int MAX_ROW_INDEX = 1048576 - 1;

    public final int minColumnIndex;
    public final int minRowIndex;
    public final int maxColumnIndex;
    public final int maxRowIndex;

    private CellRange(int minColumnIndex, int minRowIndex, int maxColumnIndex, int maxRowIndex) {
        this.minColumnIndex = minColumnIndex;
        this.minRowIndex = minRowIndex;
        this.maxColumnIndex = maxColumnIndex;
        this.maxRowIndex = maxRowIndex;
    }

    @Override
    public String toString() {
        int minRow = minRowIndex + 1;
        int maxRow = maxRowIndex + 1;
        // special case 1: Row range (e.g. 1:3)
        if(minColumnIndex == MIN_COLUMN_INDEX && maxColumnIndex == MAX_COLUMN_INDEX)
            return minRow + ":" + maxRow;

        // special case 2: Column range (e.g. A:C)
        if(minRowIndex == MIN_ROW_INDEX && maxRowIndex == MAX_ROW_INDEX)
            return columnIndexToLetter(minColumnIndex) + ":" + columnIndexToLetter(maxColumnIndex);

        return columnIndexToLetter(minColumnIndex)
                + minRow
                + ":"
                + columnIndexToLetter(maxColumnIndex)
                + maxRow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof CellRange)) return false;

        CellRange cellRange = (CellRange) o;

        return new EqualsBuilder()
                .append(minColumnIndex, cellRange.minColumnIndex)
                .append(minRowIndex, cellRange.minRowIndex)
                .append(maxColumnIndex, cellRange.maxColumnIndex)
                .append(maxRowIndex, cellRange.maxRowIndex)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(minColumnIndex)
                .append(minRowIndex)
                .append(maxColumnIndex)
                .append(maxRowIndex)
                .toHashCode();
    }

    /**
     * Construct a new CellRange with zero-based indices, like Java.
     * @param minColumn
     * @param minRow
     * @param maxColumn
     * @param maxRow
     * @return
     */
    public static CellRange fromZeroBased(int minColumn, int minRow, int maxColumn, int maxRow) {
        return new CellRange(minColumn, minRow, maxColumn, maxRow);
    }

    /**
     * Construct a new CellRange with one-based indices, like VBA.
     * @param minColumn
     * @param minRow
     * @param maxColumn
     * @param maxRow
     * @return
     */
    public static CellRange fromOneBased(int minColumn, int minRow, int maxColumn, int maxRow) {
        return new CellRange(minColumn - 1, minRow - 1, maxColumn - 1, maxRow - 1);
    }

    /**
     * Construct a new CellRange from string representation of it.
     * @param text
     * @return
     */
    public static CellRange fromString(String text) {
        Pattern gridPattern = Pattern.compile("\\$?([A-Z]+)?\\$?(\\d+)?:\\$?([A-Z]+)?\\$?(\\d+)?");
        Matcher m = gridPattern.matcher(text);
        if(m.matches()) {
            int minColumn = m.group(1) == null ? MIN_COLUMN_INDEX : columnLetterToIndex(m.group(1));
            int minRow = m.group(2) == null ? MIN_ROW_INDEX : parseInt(m.group(2)) - 1;
            int maxColumn = m.group(3) == null ? MAX_COLUMN_INDEX : columnLetterToIndex(m.group(3));
            int maxRow = m.group(4) == null ? MAX_ROW_INDEX : parseInt(m.group(4)) -1;
            return fromZeroBased(
                    minColumn
                    , minRow
                    , maxColumn
                    , maxRow
            );
        }
        return null;
    }

    public int columnCount() {
        return (maxColumnIndex - minColumnIndex) + 1;
    }

    public int rowCount() {
        return (maxRowIndex - minRowIndex) + 1;
    }
}
