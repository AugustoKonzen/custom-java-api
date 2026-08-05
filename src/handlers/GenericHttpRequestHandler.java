package handlers;

import annotations.PathVariable;
import annotations.RequestBody;
import annotations.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import enums.HttpMethod;
import exceptions.RestException;
import logger.CustomLogger;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import utils.CastingUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class GenericHttpRequestHandler implements HttpRequestHandler {

    private static final CustomLogger log = new CustomLogger(GenericHttpRequestHandler.class.getName());

    private final Object instance;
    private final Method method;
    private final HttpMethod httpMethod;

    public GenericHttpRequestHandler(Object instance, Method method, HttpMethod httpMethod) {
        this.instance = instance;
        this.method = method;
        this.httpMethod = httpMethod;
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) {
        try {
            if (!httpMethod.name().equalsIgnoreCase(request.getMethod())) {
                response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
                response.setEntity(new StringEntity("Method Not Allowed", ContentType.APPLICATION_JSON));
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            HttpEntity entity = request.getEntity();
            URIBuilder builder = new URIBuilder(request.getPath());

            Object result = method.invoke(instance, getParams(builder, mapper, entity));
            response.setCode(HttpStatus.SC_OK);
            response.setEntity(new StringEntity(mapper.writeValueAsString(result), ContentType.APPLICATION_JSON));
        } catch (Exception e) {
            log.error("Error trying handle api method", e);
            int statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            String message = e.getMessage();

            if (e instanceof RestException re) {
                statusCode = re.getStatusCode() > 0 ? re.getStatusCode() : 500;
                message = re.getMessage();
            }

            response.setCode(statusCode);
            response.setEntity(new StringEntity(message, ContentType.APPLICATION_JSON));
        }
    }

    private Object[] getParams(URIBuilder builder, ObjectMapper mapper, HttpEntity entity) throws IOException, ParseException {
        Object[] params = new Object[method.getParameterCount()];
        int i = 0;
        for (Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(RequestBody.class)) {
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    params[i] = mapper.readValue(json, param.getType());
                }
            } else if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                params[i] = builder.getQueryParams().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(requestParam.name()))
                        .map(p -> CastingUtils.castTo(param.getType(), p.getValue()))
                        .findFirst().orElseThrow();
            } else if (param.isAnnotationPresent(PathVariable.class)) {
//                 TODO: Implementar extração de variáveis de caminho, se necessário
            }
            i++;
        }
        return params;
    }
}
