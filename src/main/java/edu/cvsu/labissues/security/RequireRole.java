package edu.cvsu.labissues.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this on a controller class or method to say which roles may call it,
 * for example {@code @RequireRole("Admin")} or {@code @RequireRole({"Student", "Instructor"})}.
 * AuthInterceptor checks it before the method runs (403 if the role is not allowed).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value();
}
