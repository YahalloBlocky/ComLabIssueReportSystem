package edu.cvsu.labissues.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cvsu.labissues.model.ComputerOption;
import edu.cvsu.labissues.model.LookupBundle;
import edu.cvsu.labissues.service.LookupService;

@RestController
@RequestMapping("/api")
public class LookupController {

    private final LookupService lookups;

    public LookupController(LookupService lookups) {
        this.lookups = lookups;
    }

    @GetMapping("/lookups")
    public LookupBundle lookups() {
        return lookups.bundle();
    }

    @GetMapping("/laboratories/{id}/computers")
    public List<ComputerOption> computers(@PathVariable int id) {
        return lookups.computersOf(id);
    }
}
