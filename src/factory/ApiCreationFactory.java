package factory;

import annotations.ControllerMapping;
import annotations.Controller;
import enums.HttpMethod;
import exceptions.RestException;
import handlers.GenericHttpRequestHandler;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.protocol.UriPatternMatcher;
import org.apache.hc.core5.io.CloseMode;
import registers.ControllerRegistry;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApiCreationFactory extends GlobalCreationFactory {

    private ApiCreationFactory() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    @SuppressWarnings("deprecation")
    public static void start() {
        long startTime = System.nanoTime();
        String contextPath = getContextPath();
        int port = Integer.parseInt(prop.getProperty("server.port", "8080"));

        log.info("Starting server with context-path \"{}\"", !contextPath.isEmpty() ? contextPath : URI_DELIMITER);
        log.info("Server is using port {}", port);
        HttpClientCreationFactory.start();
        DependencyInjectionFactory.start();
        UriPatternMatcher<HttpRequestHandler> matcher = new UriPatternMatcher<>();
        registerControllers(matcher, contextPath);

        try (HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setLookupRegistry(matcher)
                .create()) {

            server.start();

            long endTime = System.nanoTime();
            long elapsedMillis = (endTime - startTime) / 1_000_000;

            if (elapsedMillis >= 1000) {
                long elapsedSeconds = elapsedMillis / 1000;
                log.info("Server started in {}s", elapsedSeconds);
            } else {
                log.info("Server started in {}ms", elapsedMillis);
            }

            AtomicBoolean running = new AtomicBoolean(true);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running.set(false);
                server.close(CloseMode.GRACEFUL);
            }));

            while (running.get()) {
                Thread.sleep(300);
            }
        } catch (Exception e) {
            log.error("Error trying to start API module", e);
            Thread.currentThread().interrupt();
            throw new RestException("Error trying to start API module");
        }
    }

    @SuppressWarnings("deprecation")
    private static void registerControllers(UriPatternMatcher<HttpRequestHandler> matcher, String contextPath) {
        try {
            for (Class<?> clazz : classes) {
                if (!clazz.isInterface() && clazz.isAnnotationPresent(Controller.class)) {
                    String controllerMapping = getControllerMapping(clazz);
                    Object instance = ControllerRegistry.get(clazz);

                    for (Method method : clazz.getDeclaredMethods()) {
                        if (hasRequestMapping(method)) {
                            HttpMethod httpMethod = getRequestMapping(method).method();
                            String uri = getUri(method);

                            matcher.register(contextPath + controllerMapping + uri,
                                    new GenericHttpRequestHandler(instance, method, httpMethod));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error trying register controllers", e);
            throw new RestException("Error trying register controllers");
        }
    }

    private static String getControllerMapping(Class<?> clazz) {
        String controllerMapping = "";
        if (clazz.isAnnotationPresent(ControllerMapping.class)) {
            controllerMapping = clazz.getAnnotation(ControllerMapping.class).value();
        }

        if (!controllerMapping.isEmpty()) {
            controllerMapping = controllerMapping.startsWith(URI_DELIMITER) ? controllerMapping : URI_DELIMITER + controllerMapping;
        }

        return controllerMapping;
    }

    private static String getContextPath() {
        String contextPath = null != prop.getProperty("server.servlet.context-path") ? prop.getProperty("server.servlet.context-path") : "";
        if (!contextPath.isEmpty()) {
            contextPath = contextPath.startsWith(URI_DELIMITER) ? contextPath : URI_DELIMITER + contextPath;
        }

        return contextPath;
    }
}
