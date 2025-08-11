package com.kivislime.filestorage;

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
    public AuthResponse register(UserCredentialsDto userCredentialsDto) {
        User user = new User();
        user.setUsername(userCredentialsDto.username());
        user.setPassword(passwordEncoder.encode(userCredentialsDto.password()));
        user.setRoles(Set.of(Role.ROLE_USER));

        try {
            userRepository.save(user);
            return new AuthResponse(user.getUsername());
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("User already exists: " + user.getUsername(), ex);
        }
    }
}
