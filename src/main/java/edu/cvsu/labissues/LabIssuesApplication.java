package edu.cvsu.labissues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the whole application: web server, database connection pool and REST API. */
@SpringBootApplication
public class LabIssuesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabIssuesApplication.class, args);
    }
}
