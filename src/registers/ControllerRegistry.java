package registers;

import enums.RegistryType;

public class ControllerRegistry extends BaseRegistry {

    private ControllerRegistry() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static <T> void register(Class<T> clazz, T instance) {
        register(clazz, instance, RegistryType.CONTROLLER);
    }

    public static <T> T get(Class<T> clazz) {
        return get(clazz, RegistryType.CONTROLLER);
    }

    public static boolean isRegistered(Class<?> clazz) {
        return isRegistered(clazz, RegistryType.CONTROLLER);
    }
}
