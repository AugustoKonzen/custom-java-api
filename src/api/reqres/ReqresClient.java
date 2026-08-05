package api.reqres;

import annotations.GetMapping;
import annotations.HttpClient;
import annotations.PathVariable;

import java.util.Map;

@HttpClient(name = "ReqRes Client", baseUrl = "${reqres.base-url}", configuration = ReqResConfig.class)
public interface ReqresClient {

    @GetMapping(value = "/api/users/{id}")
    Map<String, Object> getUser(@PathVariable("id") int id);
}
