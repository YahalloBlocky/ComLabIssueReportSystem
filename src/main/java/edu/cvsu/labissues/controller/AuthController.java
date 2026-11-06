package edu.cvsu.labissues.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.cvsu.labissues.dto.LoginRequest;
import edu.cvsu.labissues.dto.RegisterRequest;
import edu.cvsu.labissues.security.AuthInterceptor;
import edu.cvsu.labissues.security.SessionUser;
import edu.cvsu.labissues.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public SessionUser login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return startSession(http, auth.login(req));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionUser register(@Valid @RequestBody RegisterRequest req, HttpServletRequest http) {
        return startSession(http, auth.register(req));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest http) {
        HttpSession session = http.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Map.of("message", "Logged out.");
    }

    /** Who am I? (the pages call this when they open) */
    @GetMapping("/me")
    public SessionUser me(HttpServletRequest http) {
        return (SessionUser) http.getSession().getAttribute(AuthInterceptor.SESSION_KEY);
    }

    private SessionUser startSession(HttpServletRequest http, SessionUser user) {
        HttpSession old = http.getSession(false);
        if (old != null) {
            old.invalidate(); // new session id after login (prevents session fixation)
        }
        http.getSession(true).setAttribute(AuthInterceptor.SESSION_KEY, user);
        return user;
    }
}
