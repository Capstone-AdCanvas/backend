package hello.backend.user.controller;

import hello.backend.user.domain.User;
import hello.backend.user.dto.UserRegisterRequest;
import hello.backend.user.dto.UserUpdateRequest;
import hello.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "회원조회", description = "회원을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 조회 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청: 회원을 찾을 수 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(
            @Parameter(description = "조회할 회원의 ID", required = true)
            @PathVariable Long userId) {
        User user = userService.getUser(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(summary = "회원 전체조회", description = "회원을 전체조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 전체조회 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청: 조회할 사용자가 없음.")
    })
    @GetMapping()
    public ResponseEntity<List<User>> getAllUser() {
        List<User> users = userService.getAllUser();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "회원수정", description = "회원을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 수정 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청: 회원을 찾을 수 없음")
    })
    @PutMapping()
    public ResponseEntity<User> updateUser(@RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(request);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(summary = "회원삭제", description = "회원을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청: 회원을 찾을 수 없음")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<User> deleteUser(
            @Parameter(description = "조회할 회원의 ID", required = true)
            @PathVariable Long userId) {
        User user = userService.deleteUser(userId);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
