package com.kivislime.filestorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.util.Map;

public class JsonUsernamePasswordAuthenticationFilter
        extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthenticationFilter(AuthenticationManager authManager) {
        super(PathPatternRequestMatcher
                .withDefaults()
                .matcher(HttpMethod.POST, "/auth/sign-in")
        );

        setAuthenticationManager(authManager);

        setAuthenticationSuccessHandler((req, res, auth) -> {
            SecurityContextHolder.getContext().setAuthentication(auth);
            req.getSession(true);

            new HttpSessionSecurityContextRepository()
                    .saveContext(SecurityContextHolder.getContext(), req, res);

            res.setStatus(HttpStatus.OK.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);

            AuthResponse body = new AuthResponse(auth.getName());
            res.getWriter().write(objectMapper.writeValueAsString(body));
        });

        setAuthenticationFailureHandler((req, res, ex) -> {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(
                    objectMapper.writeValueAsString(Map.of("error", ex.getMessage()))
            );
        });
    }
//TODO: работает только формат передачи в x-www-form-urlencoded, в json все падает
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, password);
        return this.getAuthenticationManager().authenticate(token);
    }

}
