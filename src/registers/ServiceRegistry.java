package registers;

import enums.RegistryType;

public class ServiceRegistry extends BaseRegistry {

    private ServiceRegistry() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static <T> void register(Class<T> clazz, T instance) {
        register(clazz, instance, RegistryType.SERVICE);
    }

    public static <T> T get(Class<T> clazz) {
        return get(clazz, RegistryType.SERVICE);
    }

    public static boolean isRegistered(Class<?> clazz) {
        return isRegistered(clazz, RegistryType.SERVICE);
    }
}
