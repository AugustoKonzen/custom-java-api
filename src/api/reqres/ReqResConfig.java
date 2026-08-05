package api.reqres;

import config.HttpClientConfiguration;
import org.apache.hc.core5.http.HttpRequestInterceptor;

public class ReqResConfig implements HttpClientConfiguration {

    @Override
    public HttpRequestInterceptor requestInterceptor() {
        return new ReqresAthInterceptor();
    }
}
