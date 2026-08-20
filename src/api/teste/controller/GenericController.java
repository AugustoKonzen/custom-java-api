package api.teste.controller;

import annotations.*;
import api.teste.model.UserRequest;
import api.teste.model.UserResponse;
import api.teste.service.GenericService;
import api.visualcrossing.VisualCrossingResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Controller
@ControllerMapping(value = "/generic")
@RequiredArgsConstructor
public class GenericController {

    private final GenericService service;

    @PostMapping(value = "/find_by_email")
    public UserResponse findByEmail(@RequestBody UserRequest request) {
        return service.findByEmail(request);
    }

    @GetMapping(value = "/search_by_email")
    public UserResponse searchByEmail(@RequestParam(name = "name") String name,
                                      @RequestParam(name = "lastName") String lastName,
                                      @RequestParam(name = "email") String email) {
        return service.findByEmailAndName(name, lastName, email);
    }

    @GetMapping(value = "/reqres")
    public Map<String, Object> reqres() {
        return service.testeClient();
    }

    @GetMapping(value = "/weather")
    public VisualCrossingResponse weather(@RequestParam(name = "location") String location) {
        return service.weather(location);
    }
}
