package registers;

import enums.RegistryType;

public class ClientRegistry extends BaseRegistry {

    private ClientRegistry() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static <T> void register(Class<T> clazz, T instance) {
        register(clazz, instance, RegistryType.HTTP_CLIENT);
    }

    public static <T> T get(Class<T> clazz) {
        return get(clazz, RegistryType.HTTP_CLIENT);
    }

    public static boolean isRegistered(Class<?> clazz) {
        return isRegistered(clazz, RegistryType.HTTP_CLIENT);
    }
}
