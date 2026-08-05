package factory;

import annotations.Component;
import annotations.Controller;
import annotations.Service;
import exceptions.RestException;
import registers.ClientRegistry;
import registers.ComponentRegistry;
import registers.ControllerRegistry;
import registers.ServiceRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class DependencyInjectionFactory extends GlobalCreationFactory {

    private DependencyInjectionFactory() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static void start() {
        try {
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Component.class)) {
                    registerComponent(clazz, ComponentRegistry::isRegistered, ComponentRegistry::register);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    registerComponent(clazz, ServiceRegistry::isRegistered, ServiceRegistry::register);
                } else if (clazz.isAnnotationPresent(Controller.class)) {
                    registerComponent(clazz, ControllerRegistry::isRegistered, ControllerRegistry::register);
                }
            }
        } catch (Exception e) {
            log.error("Error trying to inject dependencies", e);
            throw new RestException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerComponent(
            Class<?> clazz,
            Predicate<Class<?>> isRegistered,
            BiConsumer<Class<Object>, Object> registerInstance
    ) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        if (isRegistered.test(clazz)) {
            return;
        }

        for (Constructor<?> constructor : clazz.getConstructors()) {
            int paramCount = constructor.getParameterCount();
            Object[] args = new Object[paramCount];
            boolean canRegister = true;

            for (int i = 0; i < paramCount; i++) {
                Class<?> dependency = constructor.getParameterTypes()[i];
                Object resolved = resolveDependency(dependency);

                if (resolved != null) {
                    args[i] = resolved;
                } else {
                    canRegister = false;
                    break;
                }
            }

            if (canRegister) {
                log.info("Registering {}", clazz.getName());
                Object instance = constructor.newInstance(args);
                setDynamicFields(instance);
                registerInstance.accept((Class<Object>) clazz, instance);
                return;
            }
        }

        throw new RestException("Couldn't instantiate class " + clazz.getName());
    }

    private static Object resolveDependency(Class<?> type)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {

        if (type.isAnnotationPresent(annotations.HttpClient.class)) {
            return ClientRegistry.get(type);
        }

        if (type.isAnnotationPresent(Service.class)) {
            registerComponent(type, ServiceRegistry::isRegistered, ServiceRegistry::register);
            return ServiceRegistry.get(type);
        }

        if (type.isAnnotationPresent(Controller.class)) {
            registerComponent(type, ControllerRegistry::isRegistered, ControllerRegistry::register);
            return ControllerRegistry.get(type);
        }

        if (type.isAnnotationPresent(Component.class)) {
            registerComponent(type, ComponentRegistry::isRegistered, ComponentRegistry::register);
            return ComponentRegistry.get(type);
        }

        return null;
    }
}
