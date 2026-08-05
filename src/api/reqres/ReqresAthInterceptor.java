package api.reqres;

import annotations.Value;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

public class ReqresAthInterceptor implements HttpRequestInterceptor {

    @Value("${reqres.token}")
    private String token;

    @Override
    public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext) {
        httpRequest.addHeader("x-api-key", token);
    }
}
