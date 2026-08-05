package annotations;

import config.DefaultHttpClientConfiguration;
import config.HttpClientConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpClient {

    String name();

    String baseUrl();

    Class<? extends HttpClientConfiguration> configuration() default DefaultHttpClientConfiguration.class;
}
