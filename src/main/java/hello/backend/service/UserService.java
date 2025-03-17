package hello.backend.service;

import hello.backend.domain.User;
import hello.backend.exception.BadRequestException;
import hello.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User registerUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        }
        else if (userRepository.existsByName(name)) {
            throw new BadRequestException("이미 사용 중인 닉네임 입니다.");
        }

        User user = new User(name, email, password);
        return userRepository.save(user);
    }
}
