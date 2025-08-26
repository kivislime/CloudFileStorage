package com.kivislime.filestorage.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kivislime.filestorage.dto.AuthResponse;
import com.kivislime.filestorage.dto.UserCredentialsRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
            log.warn("Validation failed for authentication request: {}", e.getMessage());
            throw new BadCredentialsException("Invalid authentication request", e);
        }
    }


    private AuthenticationSuccessHandler successHandler() {
        return (req, res, auth) -> {
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = req.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            log.info("User with username: {}, logged in", auth.getName());

            res.setStatus(HttpStatus.OK.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(mapper.writeValueAsString(new AuthResponse(auth.getName())));
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        return (req, res, ex) -> {
            Map<String, String> body;
            int status;

            if (ex instanceof BadCredentialsException) {
                log.warn("Anonymous user tried login: {}", ex.getMessage());
                status = HttpStatus.UNAUTHORIZED.value();
                body = Map.of("message", "Bad credentials");
            } else if (ex instanceof AuthenticationServiceException) {
                log.warn("Authentication service exception: {}", ex.getMessage());
                status = HttpStatus.BAD_REQUEST.value();
                String msg = ex.getMessage();
                if (msg == null || msg.isBlank()) {
                    msg = "Invalid authentication request";
                }
                body = Map.of("message", msg);
            } else {
                log.error("Unexpected authentication error", ex);
                status = HttpStatus.UNAUTHORIZED.value();
                body = Map.of("message", "Authentication failed");
            }

            res.setStatus(status);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(mapper.writeValueAsString(body));
        };
    }

    private void validate(UserCredentialsRequest creds) {
        Set<ConstraintViolation<UserCredentialsRequest>> violations = validator.validate(creds);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("."));
            throw new AuthenticationServiceException(msg);
        }
    }
}
