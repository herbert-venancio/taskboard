package objective.taskboard.spreadsheet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import objective.taskboard.utils.DateTimeUtils;
import objective.taskboard.utils.XmlUtils;

public final class CellValues {
    
    private CellValues() {}

    public static CellValue string(String value, Map<String, Long> sharedStrings) {
        return StringUtils.isEmpty(value) ? EMPTY : new StringCellValue(value, sharedStrings);
    }
    
    public static CellValue number(Number value) {
        return value == null ? EMPTY : new NumberCellValue(value);
    }
    
    public static CellValue bool(Boolean value) {
        return value == null ? EMPTY : new BooleanCellValue(value);
    }
    
    public static CellValue dateTime(LocalDateTime value, boolean date1904, SpreadsheetStylesEditor stylesEditor) {
        return value == null ? EMPTY : new DateTimeCellValue(value, date1904, stylesEditor);
    }

    public static CellValue date(LocalDate value, boolean date1904, SpreadsheetStylesEditor stylesEditor) {
        return value == null ? EMPTY : new DateCellValue(value, date1904, stylesEditor);
    }

    public static CellValue formula(String value) {
        return value == null ? EMPTY : new FormulaCellValue(value);
    }
    
    private static final CellValue EMPTY = new CellValue() {
        @Override
        public void writeValue(Element cell, Document sheetDoc) {
            XmlUtils.removeAllChildren(cell);
        }
    };

    private static abstract class SimpleCellValue implements CellValue {
        protected abstract String getValue();

        protected String getCellType() {
            return null;
        }
        
        protected void writeExtraAttributes(Element cell) {
        }

        @Override
        public final void writeValue(Element cell, Document sheetDoc) {
            XmlUtils.removeAllChildren(cell);

            Element valueElement = sheetDoc.createElement("v");
            valueElement.appendChild(sheetDoc.createTextNode(getValue()));
            cell.appendChild(valueElement);
            
            String cellType = getCellType();
            
            if (cellType == null) {
                cell.removeAttribute("t");
            } else {
                cell.setAttribute("t", cellType);
            }

            writeExtraAttributes(cell);
        }
    }
    
    private static class StringCellValue extends SimpleCellValue {
        private final String value;
        private final Map<String, Long> sharedStrings;
        
        public StringCellValue(String value, Map<String, Long> sharedStrings) {
            this.value = value;
            this.sharedStrings = sharedStrings;
        }

        @Override
        protected String getValue() {
            return getOrSetIndexInSharedStrings(value);
        }

        @Override
        protected String getCellType() {
            return "s";
        }

        private String getOrSetIndexInSharedStrings(String followUpDataAttrValue) {
            if (followUpDataAttrValue == null || followUpDataAttrValue.isEmpty())
                return "";

            Long index = sharedStrings.get(followUpDataAttrValue);
            if (index != null)
                return index + "";

            index = Long.valueOf(sharedStrings.size());
            sharedStrings.put(followUpDataAttrValue, index);
            return index + "";
        }
    }

    private static class NumberCellValue extends SimpleCellValue {
        private final Number value;

        public NumberCellValue(Number value) {
            this.value = value;
        }

        @Override
        protected String getValue() {
            return value.toString();
        }
    }
    
    private static class BooleanCellValue extends SimpleCellValue {
        private final Boolean value;

        public BooleanCellValue(Boolean value) {
            this.value = value;
        }

        @Override
        protected String getValue() {
            return value.toString();
        }

        @Override
        protected String getCellType() {
            return "b";
        }
    }
    
    private static class DateTimeCellValue extends SimpleCellValue {
        private final LocalDateTime value;
        private final boolean date1904;
        private final SpreadsheetStylesEditor stylesEditor;

        public DateTimeCellValue(LocalDateTime value, boolean date1904, SpreadsheetStylesEditor stylesEditor) {
            this.value = value;
            this.date1904 = date1904;
            this.stylesEditor = stylesEditor;
        }

        @Override
        protected String getValue() {
            return DateTimeUtils.toDoubleExcelFormat(value, date1904);
        }
        
        @Override
        protected void writeExtraAttributes(Element cell) {
            cell.setAttribute("s", Integer.toString(stylesEditor.getOrCreateNumberFormat("m/d/yy h:mm")));
        }
    }
    
    private static class DateCellValue extends SimpleCellValue {
        private final LocalDate value;
        private final boolean date1904;
        private final SpreadsheetStylesEditor stylesEditor;

        public DateCellValue(LocalDate value, boolean date1904, SpreadsheetStylesEditor stylesEditor) {
            this.value = value;
            this.date1904 = date1904;
            this.stylesEditor = stylesEditor;
        }

        @Override
        protected String getValue() {
            return DateTimeUtils.toDoubleExcelFormat(value.atStartOfDay(), date1904);
        }
        
        @Override
        protected void writeExtraAttributes(Element cell) {
            cell.setAttribute("s", Integer.toString(stylesEditor.getOrCreateNumberFormat("m/d/yy")));
        }
    }
    
    private static class FormulaCellValue implements CellValue {
        private final String value;
        
        public FormulaCellValue(String value) {
            this.value = value;
        }

        @Override
        public void writeValue(Element cell, Document sheetDoc) {
            XmlUtils.removeAllChildren(cell);
            
            Element valueNode = sheetDoc.createElement("f");
            valueNode.appendChild(sheetDoc.createTextNode(value));
            cell.appendChild(valueNode);

            cell.removeAttribute("t");
            cell.setAttribute("s", "4");
        }
    }

}
