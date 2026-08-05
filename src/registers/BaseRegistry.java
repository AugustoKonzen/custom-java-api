package registers;

import enums.RegistryType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseRegistry {

    protected BaseRegistry() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    private static final String INVALID_REGISTRY_TYPE = "Invalid registry type";
    private static final Map<RegistryType, Map<Class<?>, Object>> registry = new EnumMap<>(RegistryType.class);

    static {
        for (RegistryType type : RegistryType.values()) {
            registry.put(type, new HashMap<>());
        }
    }

    protected static <T> void register(Class<T> clazz, T instance, RegistryType type) {
        Map<Class<?>, Object> map = registry.get(type);
        if (map == null) {
            throw new IllegalArgumentException(INVALID_REGISTRY_TYPE);
        }
        map.put(clazz, instance);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(Class<T> clazz, RegistryType type) {
        Map<Class<?>, Object> map = registry.get(type);
        if (map == null) {
            throw new IllegalArgumentException(INVALID_REGISTRY_TYPE);
        }
        return (T) map.get(clazz);
    }

    protected static boolean isRegistered(Class<?> clazz, RegistryType type) {
        Map<Class<?>, Object> map = registry.get(type);
        if (map == null) {
            throw new IllegalArgumentException(INVALID_REGISTRY_TYPE);
        }
        return map.containsKey(clazz);
    }
}