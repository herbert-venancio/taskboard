package objective.taskboard.spreadsheet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CellRangeTest {

    @Test
    public void simpleSquareRange() {
        CellRange range = CellRange.fromString("A1:B2");
        assertDimensions(range, 0, 0, 1, 1);
    }

    @Test
    public void fixedMarkings() {
        CellRange range = CellRange.fromString("$A$1:$B$2");
        assertDimensions(range, 0, 0, 1, 1);
    }

    @Test
    public void columnsOnly() {
        CellRange range = CellRange.fromString("A:B");
        assertDimensions(range, 0, 0, 1, CellRange.MAX_ROW_INDEX);
    }

    @Test
    public void linesOnly() {
        CellRange range = CellRange.fromString("1:2");
        assertDimensions(range, 0, 0, CellRange.MAX_COLUMN_INDEX, 1);
    }

    private void assertDimensions(CellRange range, int minColumnIndex, int minRowIndex, int maxColumnIndex, int maxRowIndex) {
        assertEquals(minColumnIndex, range.minColumnIndex);
        assertEquals(minRowIndex, range.minRowIndex);
        assertEquals(maxColumnIndex, range.maxColumnIndex);
        assertEquals(maxRowIndex, range.maxRowIndex);
    }
}
