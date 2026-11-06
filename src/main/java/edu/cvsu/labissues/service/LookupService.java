package edu.cvsu.labissues.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.cvsu.labissues.model.ComputerOption;
import edu.cvsu.labissues.model.LookupBundle;
import edu.cvsu.labissues.repository.LaboratoryRepository;
import edu.cvsu.labissues.repository.LookupRepository;

@Service
public class LookupService {

    private final LookupRepository lookups;
    private final LaboratoryRepository labs;

    public LookupService(LookupRepository lookups, LaboratoryRepository labs) {
        this.lookups = lookups;
        this.labs = labs;
    }

    public LookupBundle bundle() {
        return new LookupBundle(lookups.categories(), lookups.statuses(), lookups.priorities(),
                labs.findActiveLaboratoryOptions());
    }

    public List<ComputerOption> computersOf(int laboratoryId) {
        return labs.findActiveComputerOptions(laboratoryId);
    }
}
