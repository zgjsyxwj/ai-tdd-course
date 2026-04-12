package com.example.usermanagement;

import com.example.usermanagement.dto.UpdatePwdRequest;
import com.example.usermanagement.dto.UserRequest;
import com.example.usermanagement.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPwd(request.pwd());
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername());
    }

    public UserResponse getById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return new UserResponse(user.getId(), user.getUsername());
    }

    public List<UserResponse> listAll() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername()))
                .toList();
    }

    public UserResponse updatePwd(Integer id, UpdatePwdRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setPwd(request.pwd());
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getUsername());
    }

    public void delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
