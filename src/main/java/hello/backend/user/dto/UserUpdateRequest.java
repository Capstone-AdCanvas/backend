package hello.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserUpdateRequest {
    private Long id;
    private String name;
    private String email;
    private String password;
}
