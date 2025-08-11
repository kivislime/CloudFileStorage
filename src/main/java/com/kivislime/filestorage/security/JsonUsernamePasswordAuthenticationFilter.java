package com.kivislime.filestorage.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kivislime.filestorage.dto.AuthResponse;
import com.kivislime.filestorage.dto.UserCredentialsRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonUsernamePasswordAuthenticationFilter
        extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper mapper;
    private final Validator validator;

    public JsonUsernamePasswordAuthenticationFilter(AuthenticationManager authManager, Validator validator, ObjectMapper mapper) {
        super(PathPatternRequestMatcher
                .withDefaults()
                .matcher(HttpMethod.POST, "/auth/sign-in")
        );
        this.mapper = mapper;
        this.validator = validator;
        setAuthenticationManager(authManager);
        setAuthenticationSuccessHandler(successHandler());
        setAuthenticationFailureHandler(failureHandler());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) {
        try {
            UserCredentialsRequest creds = mapper.readValue(req.getInputStream(), UserCredentialsRequest.class);
            validate(creds);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(creds.username(), creds.password());
            return getAuthenticationManager().authenticate(token);
        } catch (IOException e) {
            throw new BadCredentialsException("Invalid authentication request", e);
        }
    }


    private AuthenticationSuccessHandler successHandler() {
        return (req, res, auth) -> {
            res.setStatus(HttpStatus.OK.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(mapper.writeValueAsString(new AuthResponse(auth.getName())));
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        return (req, res, ex) -> {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(mapper.writeValueAsString(Map.of("error", ex.getMessage())));
        };
    }


    private void validate(UserCredentialsRequest creds) {
        Set<ConstraintViolation<UserCredentialsRequest>> violations = validator.validate(creds);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new AuthenticationServiceException("Validation failed: " + msg);
        }
    }
}
