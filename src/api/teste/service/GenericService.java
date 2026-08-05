package api.teste.service;

import annotations.Service;
import api.reqres.ReqresClient;
import api.teste.model.UserRequest;
import api.teste.model.UserResponse;
import api.visualcrossing.VisualCrossingClient;
import api.visualcrossing.VisualCrossingResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenericService {

    private final ReqresClient reqresClient;
    private final VisualCrossingClient visualCrossingClient;

    public UserResponse findByEmail(UserRequest request) {
        return UserResponse.builder()
                .id(UUID.randomUUID().toString())
                .name("Name")
                .lastName("LastName")
                .email(request.getEmail())
                .build();
    }

    public UserResponse findByEmailAndNome(String name, String lastName, String email) {
        return UserResponse.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .lastName(lastName)
                .email(email)
                .build();
    }

    public Map<String, Object> testeClient() {
        return reqresClient.getUser(2);
    }

    public VisualCrossingResponse weather(String location) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String time = LocalDateTime.now().format(formatter);
        return visualCrossingClient.getWeather(location, time);
    }
}
