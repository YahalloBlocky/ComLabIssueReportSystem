package edu.cvsu.labissues.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import edu.cvsu.labissues.repository.UserRepository;

/**
 * seed.sql cannot contain real BCrypt hashes, so the demo users start with a placeholder password.
 * On start-up this class replaces the placeholder with a real BCrypt hash of the demo password.
 * It only touches accounts that still have the placeholder, so changed passwords are never overwritten.
 * Turn it off with DEMO_ACCOUNTS_ENABLED=false.
 */
@Component
public class DemoAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoAccountInitializer.class);
    static final String PLACEHOLDER = "PENDING_FIRST_RUN_HASH";

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final boolean enabled;

    public DemoAccountInitializer(UserRepository users, PasswordEncoder encoder,
                                  @Value("${app.demo-accounts.enabled:true}") boolean enabled) {
        this.users = users;
        this.encoder = encoder;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        Map<String, String> demo = new LinkedHashMap<>();
        demo.put("ADMIN001", "Admin@123");
        demo.put("INST001", "Instructor@123");
        demo.put("INST002", "Instructor@123");
        demo.put("202610001", "Student@123");
        demo.put("202610002", "Student@123");
        demo.put("202610003", "Student@123");
        int updated = 0;
        for (Map.Entry<String, String> e : demo.entrySet()) {
            updated += users.replacePlaceholderPassword(e.getKey(), PLACEHOLDER, encoder.encode(e.getValue()));
        }
        if (updated > 0) {
            log.info("Demo accounts are ready ({} password(s) set).", updated);
        }
    }
}
