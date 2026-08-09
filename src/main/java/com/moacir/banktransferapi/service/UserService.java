package com.moacir.banktransferapi.service;

import com.moacir.banktransferapi.exception.DuplicateAccountException;
import com.moacir.banktransferapi.model.Role;
import com.moacir.banktransferapi.model.User;
import com.moacir.banktransferapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateAccountException(
                    "Já existe um usuário com o nome: " + username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }
}