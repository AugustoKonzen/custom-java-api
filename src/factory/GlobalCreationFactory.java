package factory;

import annotations.RequestMapping;
import annotations.Value;
import exceptions.RestException;
import logger.CustomLogger;
import utils.ClassFinder;
import utils.FindPropertyUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GlobalCreationFactory {

    protected GlobalCreationFactory() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    protected static final String URI_DELIMITER = "/";

    protected static final Properties prop;
    protected static final List<Class<?>> classes = new ArrayList<>();
    protected static final CustomLogger log = new CustomLogger(GlobalCreationFactory.class.getName());

    static {
        try (InputStream is = GlobalCreationFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new RestException("application.properties não encontrado no classpath");
            }

            prop = new Properties();
            prop.load(is);

            classes.addAll(ClassFinder.getAllClasses());
        } catch (Exception e) {
            log.error("Erro ao ler application.properties", e);
            throw new RestException("Erro ao ler application.properties");
        }
    }

    protected static String getUri(Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String uri = "";
        if (method.isAnnotationPresent(RequestMapping.class)) {
            uri = method.getAnnotation(RequestMapping.class).value();
        } else {
            Annotation annotation = Arrays.stream(method.getAnnotations()).filter(a ->
                    a.annotationType().isAnnotationPresent(RequestMapping.class)).findFirst().orElseThrow(() ->
                    new RestException("No RequestMapping Found"));
            uri = (String) annotation.annotationType().getDeclaredMethod("value").invoke(annotation);
        }

        if (!uri.isEmpty()) {
            uri = uri.startsWith(URI_DELIMITER) ? uri : URI_DELIMITER + uri;
        }

        return uri;
    }

    protected static boolean hasRequestMapping(Method method) {
        if (method.isAnnotationPresent(RequestMapping.class)) {
            return true;
        }

        return Arrays.stream(method.getAnnotations()).anyMatch(a ->
                a.annotationType().isAnnotationPresent(RequestMapping.class));
    }

    protected static RequestMapping getRequestMapping(Method method) {
        if (method.isAnnotationPresent(RequestMapping.class)) {
            return method.getAnnotation(RequestMapping.class);
        }

        return Arrays.stream(method.getAnnotations()).filter(a ->
                a.annotationType().isAnnotationPresent(RequestMapping.class)).map(a ->
                a.annotationType().getAnnotation(RequestMapping.class)).findFirst().orElseThrow(() ->
                new RestException("No RequestMapping Found"));
    }

    protected static String getProperty(String key) {
        Pattern pattern = Pattern.compile("^\\$\\{([^{}]*)}$");
        Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) {
            return FindPropertyUtils.findProperty(prop, matcher.group(1));
        } else {
            return key;
        }
    }

    protected static void setDynamicFields(Object instance) throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                Value value = field.getAnnotation(Value.class);
                Object content = field.getType().cast(getProperty(value.value()));
                field.setAccessible(true);
                field.set(instance, content);
            }
        }
    }
}
