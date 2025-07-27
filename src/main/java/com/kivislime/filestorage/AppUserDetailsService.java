package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    //TODO: Transactional?? Заменить при появлении ролей   List.of()
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> new UserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        List.of()
                ))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
