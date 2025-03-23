package hello.backend.user.service;

import hello.backend.user.domain.User;
import hello.backend.exception.BadRequestException;
import hello.backend.exception.NotFoundException;
import hello.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    //회원가입
    @Transactional
    public User registerUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        }
        else if (userRepository.existsByName(name)) {
            throw new BadRequestException("이미 사용 중인 닉네임 입니다.");
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
                .orElseThrow(() -> new NotFoundException("해당 id를 조회할 수 없습니다."));

        return user;
    }

    //회원 전체조회
    @Transactional
    public List<User> getAllUser() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            throw new NotFoundException("해당 id를 조회할 수 없습니다.");
        }

        return users;
    }

    //회원수정
    @Transactional
    public User updateUser(Long id, String name, String email, String password) {
        User user = userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("해당 id를 조회할 수 없습니다."));

        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        return user;
    }

    //회원삭제
    @Transactional
    public User deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id를 조회할 수 없습니다."));
        userRepository.delete(user);

        return user;
    }
}
