package itis.kpfu.service;

import itis.kpfu.exception.UserExistsException;
import itis.kpfu.exception.UserNotFoundException;
import itis.kpfu.model.UserEntity;
import itis.kpfu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void registerUser(UserEntity user) {
        if (userRepository.findById(user.getId()).isPresent()) {
            log.warn("Пользователь уже существует с id %d".formatted(user.getId()));
            throw new UserExistsException(user.getId());
        }
        userRepository.save(user);
    }

    public UserEntity findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public void unregisterUser(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(user.get().getId());
        } else {
            throw new UserExistsException(id);
        }
    }


}
