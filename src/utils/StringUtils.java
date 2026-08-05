package utils;

public class StringUtils {

    private StringUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static String format(String str, Object... args) {
        if (null != args) {
            for (Object arg : args) {
                if (null != arg && !(arg instanceof Throwable)) {
                    str = str.replaceFirst("\\{}", arg.toString());
                }
            }
        }

        return str;
    }

    public static String capitalize(String str) {
        if (null == str || str.isEmpty()) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar = Character.toUpperCase(baseChar);

        if (baseChar == updatedChar) {
            return str;
        } else {
            char[] chars = str.toCharArray();
            chars[0] = updatedChar;
            return new String(chars, 0, chars.length);
        }
    }
}
