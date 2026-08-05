package config;

import interceptors.DefaultClientRequestInterceptor;
import interceptors.DefaultClientResponseInterceptor;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;

public interface HttpClientConfiguration {

    default HttpRequestInterceptor requestInterceptor() {
        return new DefaultClientRequestInterceptor();
    }

    default HttpResponseInterceptor responseInterceptor() {
        return new DefaultClientResponseInterceptor();
    }
}
