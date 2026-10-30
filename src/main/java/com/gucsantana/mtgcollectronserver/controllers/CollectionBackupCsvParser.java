package com.gucsantana.mtgcollectronserver.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class CollectionBackupCsvParser {

    private static final List<String> CARD_FIELDS = List.of(
            "card_name", "set_code", "card_count", "collector_number",
            "is_considered_extra", "is_foil", "is_tagged_circle", "is_tagged_cross",
            "is_tagged_square", "is_tagged_triangle");
    private static final List<String> SET_FIELDS = List.of(
            "set_code", "base_set_owned", "commons_count", "extra_set_owned", "foils_owned",
            "full_set_base_only", "full_set_every_single", "full_set_one_each",
            "mythics_count", "rares_count", "uncommons_count");

    private CollectionBackupCsvParser() {
    }

    static ParsedBackup parse(InputStream input) throws IOException {
        List<List<String>> rows = parseRecords(readContents(input));
        if (rows.isEmpty() || !isMarker(rows.get(0), "card_info")) {
            throw badRequest("The CSV must start with the card_info section");
        }

        int index = 1;
        if (index >= rows.size()) {
            throw badRequest("The card_info section is missing its header");
        }
        Header cardHeader = header(rows.get(index++), CARD_FIELDS, "card_info");
        List<CardRow> cards = new ArrayList<>();
        Set<CardKey> cardKeys = new HashSet<>();
        boolean foundSetSection = false;
        for (; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (isMarker(row, "set_info")) {
                foundSetSection = true;
                index++;
                break;
            }
            requireWidth(row, cardHeader, "card_info");
            String cardName = required(row, cardHeader, "card_name", "card_info");
            String setCode = required(row, cardHeader, "set_code", "card_info");
            String collectorNumber = required(row, cardHeader, "collector_number", "card_info");
            if (cardName.length() > 255 || setCode.length() > 10 || collectorNumber.length() > 20) {
                throw badRequest("A card name, set code, or collector number exceeds the supported length");
            }
            int count = parseNonNegativeInt(required(row, cardHeader, "card_count", "card_info"),
                    "card_count");
            boolean extra = parseBoolean(required(row, cardHeader, "is_considered_extra", "card_info"),
                    "is_considered_extra");
            boolean foil = parseBoolean(required(row, cardHeader, "is_foil", "card_info"), "is_foil");
            boolean circle = parseBoolean(required(row, cardHeader, "is_tagged_circle", "card_info"),
                    "is_tagged_circle");
            boolean cross = parseBoolean(required(row, cardHeader, "is_tagged_cross", "card_info"),
                    "is_tagged_cross");
            boolean square = parseBoolean(required(row, cardHeader, "is_tagged_square", "card_info"),
                    "is_tagged_square");
            boolean triangle = parseBoolean(required(row, cardHeader, "is_tagged_triangle", "card_info"),
                    "is_tagged_triangle");
            CardKey key = new CardKey(setCode, collectorNumber);
            if (!cardKeys.add(key)) {
                throw badRequest("The card_info section contains a duplicate set/collector-number key");
            }
            cards.add(new CardRow(cardName, setCode, count, collectorNumber, extra, foil,
                    circle, cross, square, triangle));
        }
        if (!foundSetSection) {
            throw badRequest("The CSV is missing the set_info section");
        }
        if (index >= rows.size()) {
            throw badRequest("The set_info section is missing its header");
        }

        Header setHeader = header(rows.get(index++), SET_FIELDS, "set_info");
        List<SetRow> sets = new ArrayList<>();
        Set<String> setCodes = new HashSet<>();
        for (; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            requireWidth(row, setHeader, "set_info");
            String setCode = required(row, setHeader, "set_code", "set_info");
            if (setCode.length() > 10) {
                throw badRequest("A set code exceeds the supported length");
            }
            if (!setCodes.add(setCode)) {
                throw badRequest("The set_info section contains a duplicate set code");
            }
            sets.add(new SetRow(
                    setCode,
                    parseNonNegativeLong(required(row, setHeader, "base_set_owned", "set_info"),
                            "base_set_owned"),
                    parseNonNegativeLong(required(row, setHeader, "commons_count", "set_info"),
                            "commons_count"),
                    parseNonNegativeLong(required(row, setHeader, "extra_set_owned", "set_info"),
                            "extra_set_owned"),
                    parseNonNegativeLong(required(row, setHeader, "foils_owned", "set_info"),
                            "foils_owned"),
                    parseOptionalPercentage(value(row, setHeader, "full_set_base_only")),
                    parseOptionalPercentage(value(row, setHeader, "full_set_every_single")),
                    parseOptionalPercentage(value(row, setHeader, "full_set_one_each")),
                    parseNonNegativeLong(required(row, setHeader, "mythics_count", "set_info"),
                            "mythics_count"),
                    parseNonNegativeLong(required(row, setHeader, "rares_count", "set_info"),
                            "rares_count"),
                    parseNonNegativeLong(required(row, setHeader, "uncommons_count", "set_info"),
                            "uncommons_count")));
        }
        if (sets.isEmpty()) {
            throw badRequest("The set_info section must contain at least one set row");
        }
        return new ParsedBackup(List.copyOf(cards), List.copyOf(sets));
    }

    private static String readContents(InputStream input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
                StringWriter contents = new StringWriter()) {
            reader.transferTo(contents);
            String csv = contents.toString();
            return csv.startsWith("\uFEFF") ? csv.substring(1) : csv;
        }
    }

    private static List<List<String>> parseRecords(String contents) {
        List<List<String>> records = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;

        for (int i = 0; i < contents.length(); i++) {
            char character = contents.charAt(i);
            if (quoted) {
                if (character == '"') {
                    if (i + 1 < contents.length() && contents.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        quoteClosed = true;
                    }
                } else {
                    field.append(character);
                }
                continue;
            }

            if (quoteClosed && character != ',' && character != '\r' && character != '\n') {
                throw badRequest("The CSV contains characters after a closing quote");
            }
            if (character == '"' && field.isEmpty() && !quoteClosed) {
                quoted = true;
            } else if (character == '"') {
                throw badRequest("The CSV contains a quote inside an unquoted field");
            } else if (character == ',') {
                row.add(field.toString());
                field.setLength(0);
                quoteClosed = false;
            } else if (character == '\r' || character == '\n') {
                row.add(field.toString());
                addNonEmptyRecord(records, row);
                row = new ArrayList<>();
                field.setLength(0);
                quoteClosed = false;
                if (character == '\r' && i + 1 < contents.length()
                        && contents.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                field.append(character);
            }
        }
        if (quoted) {
            throw badRequest("The CSV contains an unterminated quoted field");
        }
        if (!row.isEmpty() || !field.isEmpty() || quoteClosed) {
            row.add(field.toString());
            addNonEmptyRecord(records, row);
        }
        return records;
    }

    private static void addNonEmptyRecord(List<List<String>> records, List<String> row) {
        if (row.stream().anyMatch(value -> !value.isEmpty())) {
            records.add(row);
        }
    }

    private static Header header(List<String> row, List<String> requiredFields, String section) {
        Map<String, Integer> positions = new java.util.HashMap<>();
        for (int index = 0; index < row.size(); index++) {
            String name = row.get(index);
            if (name.isBlank() || positions.putIfAbsent(name, index) != null) {
                throw badRequest("The " + section + " header contains a blank or duplicate field");
            }
        }
        List<String> missing = requiredFields.stream()
                .filter(name -> !positions.containsKey(name))
                .toList();
        if (!missing.isEmpty()) {
            throw badRequest("The " + section + " header is missing required fields: "
                    + String.join(", ", missing));
        }
        return new Header(positions, row.size());
    }

    private static void requireWidth(List<String> row, Header header, String section) {
        if (row.size() != header.width()) {
            throw badRequest("A row in " + section + " has an unexpected number of fields");
        }
    }

    private static String required(List<String> row, Header header, String field, String section) {
        String value = value(row, header, field);
        if (value.isBlank()) {
            throw badRequest("A required " + field + " value is missing from " + section);
        }
        return value;
    }

    private static String value(List<String> row, Header header, String field) {
        return row.get(header.positions().get(field));
    }

    private static int parseNonNegativeInt(String value, String field) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw badRequest("Invalid non-negative integer for " + field);
        }
    }

    private static long parseNonNegativeLong(String value, String field) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed < 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw badRequest("Invalid non-negative integer for " + field);
        }
    }

    private static Float parseOptionalPercentage(String value) {
        if (value.isBlank()) {
            return null;
        }
        try {
            float parsed = Float.parseFloat(value);
            if (!Float.isFinite(parsed) || parsed < 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw badRequest("Invalid percentage value in the set_info section");
        }
    }

    private static boolean parseBoolean(String value, String field) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw badRequest("Invalid boolean for " + field);
    }

    private static boolean isMarker(List<String> row, String marker) {
        return row.size() == 1 && marker.equals(row.get(0));
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    record ParsedBackup(List<CardRow> cards, List<SetRow> sets) {
    }

    record CardRow(
            String cardName,
            String setCode,
            int count,
            String collectorNumber,
            boolean extra,
            boolean foil,
            boolean taggedCircle,
            boolean taggedCross,
            boolean taggedSquare,
            boolean taggedTriangle) {
    }

    record SetRow(
            String setCode,
            long baseSetOwned,
            long commonsCount,
            long extraSetOwned,
            long foilsOwned,
            Float fullSetBaseOnly,
            Float fullSetEverySingle,
            Float fullSetOneEach,
            long mythicsCount,
            long raresCount,
            long uncommonsCount) {
    }

    record CardKey(String setCode, String collectorNumber) {
    }

    private record Header(Map<String, Integer> positions, int width) {
    }
}
