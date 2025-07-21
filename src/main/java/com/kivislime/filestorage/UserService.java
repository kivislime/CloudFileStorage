package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //TODO: Transactional??
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.emptyList()
                ))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

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

    @Transactional
    public AuthResponse login(UserCredentialsDto userCredentialsDto) {
        User user = userRepository.findByUsername(userCredentialsDto.username())
                .orElseThrow(() -> new UsernameNotFoundException(userCredentialsDto.username()));

        //TODO: заменить на свою ошибку? BadCredentialsException
        if (!passwordEncoder.matches(userCredentialsDto.password(), user.getPassword())) {
            throw new BadCredentialsException("Entered wrong password from user: " + user.getUsername());
        }

        return new AuthResponse(user.getUsername());
    }
}
