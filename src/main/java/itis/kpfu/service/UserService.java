package itis.kpfu.service;

import itis.kpfu.exception.UserNotFoundException;
import itis.kpfu.model.UserEntity;
import itis.kpfu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void registerUser(UserEntity user) {
        userRepository.save(user);
    }

    public UserEntity findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public void unregisterUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(user.getId());
    }


}
