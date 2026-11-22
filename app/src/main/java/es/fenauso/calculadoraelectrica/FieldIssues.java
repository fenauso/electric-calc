package es.fenauso.calculadoraelectrica;

import java.util.LinkedHashSet;
import java.util.Set;

final class FieldIssues {
    private FieldIssues() {}

    static String warning(String keys, String text) {
        return "\n! " + keys + "|" + text;
    }

    static Set<String> keys(String answer) {
        Set<String> keys = new LinkedHashSet<>();
        for (String line : answer.split("\\n")) {
            int divider = line.indexOf('|');
            if (line.startsWith("! ") && divider > 2) {
                for (String key : line.substring(2, divider).split(" ")) keys.add(key);
            }
        }
        return keys;
    }

    static String message(String warning) {
        return warning.substring(warning.indexOf('|') + 1);
    }

    static IllegalArgumentException invalid(String keys, String message) {
        return new Invalid(keys, message);
    }

    static final class Invalid extends IllegalArgumentException {
        final String[] keys;

        Invalid(String keys, String message) {
            super(message);
            this.keys = keys.split(" ");
        }
    }
}
