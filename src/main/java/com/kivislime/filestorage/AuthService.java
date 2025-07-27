package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    //TODO: вынести методы в AuthService нижележащие
    // Регистрацию и аутентификацию разделяют? Но есть ли смысл для пары методов то? Как будто бы нет
    @Transactional
    public AuthResponse register(UserCredentialsDto userCredentialsDto) {
        User user = new User();
        user.setUsername(userCredentialsDto.username());
        user.setPassword(passwordEncoder.encode(userCredentialsDto.password()));

        try {
            userRepository.save(user);
            return new AuthResponse(user.getUsername());
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("User already exists: " + user.getUsername(), ex);
        }
    }
}
