package api.visualcrossing;

import annotations.GetMapping;
import annotations.HttpClient;
import annotations.PathVariable;

@HttpClient(name = "VisualCrossing Client", baseUrl = "${visualcrossing.api.base-url}", configuration = VisualCrossingConfig.class)
public interface VisualCrossingClient {

    @GetMapping(value = "/VisualCrossingWebServices/rest/services/timeline/{location}/{time}")
    VisualCrossingResponse getWeather(@PathVariable("location") String location, @PathVariable("time") String time);
}
