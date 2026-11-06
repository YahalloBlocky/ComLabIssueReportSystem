package edu.cvsu.labissues.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * The "security guard" of the API. It runs BEFORE every /api/** controller method
 * (except login, register and health, see WebConfig):
 *   1. Is someone logged in?           no  -> 401 Unauthorized
 *   2. Does the method have @RequireRole and is the person's role allowed?   no -> 403 Forbidden
 * Because the check is on the server, hiding a button in the browser is never the only protection.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** The name under which the logged-in user is stored in the session. */
    public static final String SESSION_KEY = "currentUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!(handler instanceof HandlerMethod method)) {
            return true; // not a controller method (for example a static file)
        }

        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(SESSION_KEY);
        if (user == null) {
            writeError(response, 401, "Unauthorized", "Please log in first.");
            return false;
        }

        RequireRole required = method.getMethodAnnotation(RequireRole.class);
        if (required == null) {
            required = method.getBeanType().getAnnotation(RequireRole.class);
        }
        if (required != null && !Arrays.asList(required.value()).contains(user.role())) {
            writeError(response, 403, "Forbidden", "Your role is not allowed to do this.");
            return false;
        }
        return true;
    }

    private void writeError(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error
                + "\",\"message\":\"" + message + "\",\"fieldErrors\":null}");
    }
}
