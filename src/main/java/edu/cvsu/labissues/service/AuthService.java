package edu.cvsu.labissues.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.cvsu.labissues.dto.LoginRequest;
import edu.cvsu.labissues.dto.RegisterRequest;
import edu.cvsu.labissues.exception.ApiException;
import edu.cvsu.labissues.model.UserAccount;
import edu.cvsu.labissues.repository.UserRepository;
import edu.cvsu.labissues.security.SessionUser;

/** Login and registration rules. */
@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public SessionUser login(LoginRequest req) {
        // Same message for "no such user" and "wrong password" so nobody can guess which IDs exist.
        UserAccount account = users.findBySchoolId(req.schoolId().trim())
                .orElseThrow(() -> ApiException.unauthorized("Wrong ID number or password."));
        if (!encoder.matches(req.password(), account.passwordHash())) {
            throw ApiException.unauthorized("Wrong ID number or password.");
        }
        return new SessionUser(account.userId(), account.role(), account.fullName());
    }

    public SessionUser register(RegisterRequest req) {
        String role = req.role().trim();
        if (!SessionUser.ROLE_STUDENT.equals(role) && !SessionUser.ROLE_INSTRUCTOR.equals(role)) {
            throw ApiException.badRequest("You can only register as a Student or an Instructor.");
        }
        String schoolId = req.schoolId().trim();
        String email = req.email().trim().toLowerCase();
        if (users.existsBySchoolId(schoolId)) {
            throw ApiException.conflict("That ID number is already registered.");
        }
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("That email address is already registered.");
        }
        int roleId = users.findRoleId(role).orElseThrow(() -> ApiException.badRequest("Unknown role."));
        String first = req.firstName().trim();
        String last = req.lastName().trim();
        int id = users.insert(roleId, schoolId, first, last, email, encoder.encode(req.password()));
        return new SessionUser(id, role, first + " " + last);
    }
}
