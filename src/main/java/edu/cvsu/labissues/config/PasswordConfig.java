package edu.cvsu.labissues.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * How passwords are stored is chosen by the setting app.password-mode (environment variable PASSWORD_MODE):
 *   plain  (default) - the password is stored exactly as typed, so it can be read in MySQL Workbench.
 *                      Simple for a class demo, but NOT safe for real users.
 *   bcrypt           - the password is stored as an irreversible salted hash (recommended for real use).
 * Passwords saved in one mode do not work in the other mode.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder(@Value("${app.password-mode:plain}") String mode) {
        if ("bcrypt".equalsIgnoreCase(mode)) {
            return new BCryptPasswordEncoder();
        }
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence raw) {
                return raw.toString();
            }

            @Override
            public boolean matches(CharSequence raw, String stored) {
                return stored != null && stored.contentEquals(raw);
            }
        };
    }
}
