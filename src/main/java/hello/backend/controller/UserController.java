package hello.backend.controller;

import hello.backend.domain.User;
import hello.backend.dto.UserRegisterRequest;
import hello.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 중복된 이메일 또는 닉네임")
    })
    @PostMapping()
    public ResponseEntity<User> registerUser(@RequestBody UserRegisterRequest request) {
        User createUser = userService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return new ResponseEntity<>(createUser, HttpStatus.CREATED);
    }
}
