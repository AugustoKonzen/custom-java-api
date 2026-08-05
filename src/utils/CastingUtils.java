package utils;

public class CastingUtils {

    private CastingUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static Object castTo(Class<?> targetType, String value) {
        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        // Adicione outros tipos conforme necessário
        throw new IllegalArgumentException("Unsupported type: " + targetType.getName());
    }
}
