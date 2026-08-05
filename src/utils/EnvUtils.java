package utils;

public class EnvUtils {

    private EnvUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        return null != value ? value : defaultValue;
    }
}
