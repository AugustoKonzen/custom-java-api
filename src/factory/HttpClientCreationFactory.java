package factory;

import annotations.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.HttpClientConfiguration;
import exceptions.RestException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;
import registers.ClientRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

public class HttpClientCreationFactory extends GlobalCreationFactory {

    private HttpClientCreationFactory() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static void start() {
        try {
            for (Class<?> clazz : classes) {
                if (clazz.isInterface() && clazz.isAnnotationPresent(HttpClient.class)) {
                    HttpClient httpClient = clazz.getAnnotation(HttpClient.class);
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (hasRequestMapping(method)) {
                            registerHttpClient(method, httpClient);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error trying create HttpClient", e);
            throw new RestException("Error trying create HttpClient");
        }
    }

    private static void registerHttpClient(Method targetMethod, HttpClient httpClient) {
        Class<?> clientInterface = targetMethod.getDeclaringClass();

        Object proxy = Proxy.newProxyInstance(
                clientInterface.getClassLoader(),
                new Class[]{clientInterface},
                (proxyInstance, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> clientInterface.getName() + " Proxy";
                            case "hashCode" -> System.identityHashCode(proxyInstance);
                            case "equals" -> proxyInstance == args[0];
                            default -> null;
                        };
                    }

                    RequestMapping requestMapping = getRequestMapping(method);
                    String baseUrl = getProperty(httpClient.baseUrl());

                    URIBuilder uriBuilder = new URIBuilder(baseUrl);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    if (args == null) args = new Object[0];
                    HttpClientConfiguration configuration = httpClient.configuration().getConstructor().newInstance();
                    HttpRequestInterceptor requestInterceptor = configuration.requestInterceptor();
                    HttpResponseInterceptor responseInterceptor = configuration.responseInterceptor();
                    setDynamicFields(requestInterceptor);
                    setDynamicFields(responseInterceptor);
                    String body = buildUrlAndGetBody(uriBuilder, method, method.getParameters(), args, mapper);
                    return executeRequest(uriBuilder.build().toString(), requestMapping, body, method, mapper,
                            requestInterceptor, responseInterceptor);
                }
        );

        @SuppressWarnings("unchecked")
        Class<Object> type = (Class<Object>) clientInterface;
        ClientRegistry.register(type, proxy);
    }

    private static String buildUrlAndGetBody(URIBuilder uriBuilder, Method method, Parameter[] parameters, Object[] args, ObjectMapper mapper)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JsonProcessingException {

        String body = null;
        String uri = getUri(method);
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                String name = requestParam.name();
                String value = args[i] != null ? String.valueOf(args[i]) : "";
                uriBuilder.addParameter(name, value);
            } else if (param.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVariable = param.getAnnotation(PathVariable.class);
                String name = pathVariable.value();
                uri = uri.replace("{" + name + "}", String.valueOf(args[i]));
            } else if (param.isAnnotationPresent(RequestBody.class) && null != args[i]) {
                body = mapper.writeValueAsString(args[i]);
            }
        }

        if (!uri.isEmpty() && !uri.trim().equals("/")) {
            uriBuilder.appendPath(uri);
        }
        return body;
    }

    private static Object executeRequest(String finalUrl, RequestMapping requestMapping, String body, Method method,
                                         ObjectMapper mapper, HttpRequestInterceptor requestInterceptor, HttpResponseInterceptor responseInterceptor) {
        ClassicRequestBuilder builder = ClassicRequestBuilder.create(requestMapping.method().name())
                .setUri(finalUrl);
        ClassicHttpRequest request = builder.build();
        if (null != body) {
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        }

        try (CloseableHttpClient client = HttpClients.custom()
                .addRequestInterceptorFirst(requestInterceptor)
                .addResponseInterceptorFirst(responseInterceptor)
                .build()) {
            ClassicHttpResponse response = client.executeOpen(null, request, null);
            int statusCode = response.getCode();
            String jsonResponse = EntityUtils.toString(response.getEntity());
            Class<?> returnType = method.getReturnType();
            if (statusCode >= 200 && statusCode < 300) {
                return mapper.readValue(jsonResponse, returnType);
            }

            throw new RestException(jsonResponse, statusCode);
        } catch (Exception e) {
            log.error("Error trying execute request {}", finalUrl, e);
            throw new RestException(e);
        }
    }
}
