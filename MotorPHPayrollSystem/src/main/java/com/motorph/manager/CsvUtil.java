package com.motorph.manager;

import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    private CsvUtil() {
    }

    public static String[] parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (character == ',' && !insideQuotes) {
                columns.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(character);
            }
        }

        columns.add(currentField.toString().trim());
        return columns.toArray(new String[0]);
    }

    public static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    public static String toCsvRow(String... values) {
        StringBuilder row = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            row.append(escapeCsv(values[i]));

            if (i < values.length - 1) {
                row.append(",");
            }
        }

        return row.toString();
    }
}
