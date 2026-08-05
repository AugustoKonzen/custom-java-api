package utils;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindPropertyUtils {

    private FindPropertyUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    private static final String REGEX = "^\\$\\{([^{}]*)}$";

    public static String findProperty(Properties prop, String key) {
        return findProperty(prop, key, null);
    }

    public static String findProperty(Properties prop, String key, String defaultValue) {
        Pattern pattern = Pattern.compile(REGEX);
        String value = prop.getProperty(key, defaultValue);
        if (null == value) {
            return null;
        }

        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            return EnvUtils.get(matcher.group(1));
        } else {
            return value;
        }
    }
}
