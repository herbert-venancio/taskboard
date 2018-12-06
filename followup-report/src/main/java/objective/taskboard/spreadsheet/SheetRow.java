package objective.taskboard.spreadsheet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface SheetRow {

    void addColumn(String value);
    void addColumn(Number value);
    void addColumn(Boolean value);
    void addColumn(LocalDateTime value);
    void addColumn(LocalDate value);
    void addFormula(String formula);

    void setValue(String columnLetter, String value);
    void setValue(String columnLetter, Number value);
    void setValue(String columnLetter, Boolean value);
    void setValue(String columnLetter, LocalDateTime value);
    void setValue(String columnLetter, LocalDate value);
    void setFormula(String columnLetter, String value);

    default void addColumn(ZonedDateTime value) {
        addColumn(value == null ? null : value.toLocalDateTime());
    }
}