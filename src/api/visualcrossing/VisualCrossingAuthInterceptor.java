package api.visualcrossing;

import annotations.Value;
import exceptions.RestException;
import logger.CustomLogger;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

public class VisualCrossingAuthInterceptor implements HttpRequestInterceptor {

    private static final CustomLogger log = new CustomLogger(VisualCrossingAuthInterceptor.class.getName());

    @Value("${visualcrossing.api.key}")
    private String token;

    @Override
    public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
        try {
            URIBuilder uri = new URIBuilder(httpRequest.getUri())
                    .setParameter("lang", "pt")
                    .setParameter("unitGroup", "metric")
                    .setParameter("key", token)
                    .setParameter("include", "current")
                    .setParameter("contentType", "json");
            httpRequest.setUri(uri.build());
        } catch (URISyntaxException e) {
            log.error("Error trying to add parameters in weather request", e);
            throw new RestException(e);
        }
    }
}
