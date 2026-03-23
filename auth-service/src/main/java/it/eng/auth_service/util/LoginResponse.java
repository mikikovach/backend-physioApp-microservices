package it.eng.auth_service.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class LoginResponse {

    private String token;
    private long expiresIn;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
}
