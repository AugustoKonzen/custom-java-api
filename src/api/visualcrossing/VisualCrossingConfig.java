package api.visualcrossing;

import config.HttpClientConfiguration;
import org.apache.hc.core5.http.HttpRequestInterceptor;

public class VisualCrossingConfig implements HttpClientConfiguration {

    @Override
    public HttpRequestInterceptor requestInterceptor() {
        return new VisualCrossingAuthInterceptor();
    }
}
