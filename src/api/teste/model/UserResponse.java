package api.teste.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class UserResponse {

    private String id;
    private String name;
    private String lastName;
    private String email;
}
