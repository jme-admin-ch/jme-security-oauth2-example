package ch.admin.bit.jeap.jme.security.oauth.client;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class SanitizeUtil {

    private static final Pattern LOG_INJECTION_PATTERN =
            Pattern.compile("[\r\n\t]");

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return LOG_INJECTION_PATTERN.matcher(input)
                .replaceAll("_");  // Or "" if you prefer removal
    }
}
