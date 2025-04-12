package hello.backend.user.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.ErrorResponse;
import hello.backend.error.exception.BusinessException;
import hello.backend.user.domain.User;
import hello.backend.user.dto.UserUpdateRequest;
import hello.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    List<ErrorResponse.FieldError> errors = new ArrayList<>();

    //회원가입
    @Transactional
    public User registerUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            errors.add(new ErrorResponse.FieldError("email", email, "이미 사용 중인 이메일입니다."));
        }
        if(userRepository.existsByName(name)) {
            errors.add(new ErrorResponse.FieldError("name", name, "이미 사용 중인 닉네임 입니다."));
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_USER_INPUT, errors);
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();

        return userRepository.save(user);
    }

    //회원조회
    @Transactional
    public User getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return user;
    }

    //회원 전체조회
    @Transactional
    public List<User> getAllUser() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_LIST_EMPTY);
        }

        return users;
    }

    //회원수정
    @Transactional
    public User updateUser(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.update(request);

        return user;
    }

    //회원삭제
    @Transactional
    public User deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);

        return user;
    }
}
