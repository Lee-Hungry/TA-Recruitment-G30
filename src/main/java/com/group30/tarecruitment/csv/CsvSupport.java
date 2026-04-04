package com.group30.tarecruitment.csv;

import java.util.ArrayList;
import java.util.List;

public final class CsvSupport {

    private CsvSupport() {
    }

    public static String joinRow(String... values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            escaped.add(escape(value));
        }
        return String.join(",", escaped);
    }

    public static List<String> parseRow(String line) {
        List<String> values = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            values.add("");
            return values;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }

        values.add(current.toString());
        return values;
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        boolean needsQuotes = safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r");
        if (!needsQuotes) {
            return safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
