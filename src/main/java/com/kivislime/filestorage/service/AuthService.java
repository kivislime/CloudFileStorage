package com.kivislime.filestorage.service;

import com.kivislime.filestorage.dto.AuthResponse;
import com.kivislime.filestorage.dto.UserCredentialsRequest;
import com.kivislime.filestorage.entity.User;
import com.kivislime.filestorage.exception.UserAlreadyExistsException;
import com.kivislime.filestorage.repository.UserRepository;
import com.kivislime.filestorage.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public AuthResponse register(UserCredentialsRequest userCredentialsRequest) {
        User user = new User();
        user.setUsername(userCredentialsRequest.username());
        user.setPassword(passwordEncoder.encode(userCredentialsRequest.password()));
        user.setRoles(Set.of(Role.ROLE_USER));

        try {
            userRepository.save(user);
            return new AuthResponse(user.getUsername());
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("User already exists: " + user.getUsername(), ex);
        }
    }
}
