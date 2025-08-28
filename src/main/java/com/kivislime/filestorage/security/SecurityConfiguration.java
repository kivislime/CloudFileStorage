package com.kivislime.filestorage.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kivislime.filestorage.config.CorsConfig;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final CorsConfig corsConfig;
    private final ObjectMapper mapper;
    private final Validator validator;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        JsonUsernamePasswordAuthenticationFilter jsonFilter = new JsonUsernamePasswordAuthenticationFilter(authManager, validator, mapper);
        return http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/sign-out")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((req, res, auth) -> {
                            boolean isAnonymous = (auth == null)
                                    || !auth.isAuthenticated()
                                    || auth instanceof AnonymousAuthenticationToken
                                    || "anonymousUser".equals(auth.getPrincipal());

                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            if (isAnonymous) {
                                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                                var body = Map.of("message", "Not logged in");
                                mapper.writeValue(res.getWriter(), body);
                                res.getWriter().flush();
                            } else {
                                log.info("User with username: {}, is logged out", auth.getName());
                                res.setStatus(HttpStatus.NO_CONTENT.value());
                            }
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/sign-in", "/auth/sign-up").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
